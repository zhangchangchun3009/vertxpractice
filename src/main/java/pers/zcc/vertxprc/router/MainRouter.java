package pers.zcc.vertxprc.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import io.vertx.mysqlclient.MySQLPool;
import pers.zcc.vertxprc.common.constant.Constants;
import pers.zcc.vertxprc.common.vo.Response;

public class MainRouter implements IRouterCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainRouter.class);

    private final JsonObject config;

    private final MySQLPool dbPool;

    public MainRouter(JsonObject config, MySQLPool dbPool) {
        this.config = config;
        this.dbPool = dbPool;
    }

    @Override
    public Router getRouter(Vertx vertx) {
        String webContextPath = config.getString("server.contextPath");
        Router router = Router.router(vertx);
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx))).handler(ctx -> {
            ctx.put(Constants.APPLICATION_CONFIG, config);
            ctx.put(Constants.MYSQL_POOL, dbPool);
            ctx.next();
        }).failureHandler(ctx -> {
            LOGGER.error("uncaught exception,", ctx.failure());
            ctx.json(new Response<String>().fail("500", "server internal error"));
        });

        router.route().handler(CorsHandler.create("vertx.io").allowedMethod(HttpMethod.GET));
        router.route().handler(FaviconHandler.create(vertx));

        StaticHandler staticHandler = StaticHandler.create("webroot");
        staticHandler.setIndexPage("index.html");
        router.route("/").handler(staticHandler);

        router.route("/static/*").handler(staticHandler);

        TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);

        router.route("/dynamic/*").handler(ctx -> {
            ctx.data().put("name", ctx.request().getParam("name"));
            ctx.data().put("site", "vert.x");
            ctx.next();
        }).handler(TemplateHandler.create(engine));

        router.get("/localized").handler(rc -> {
            //虽然通过一个 switch 循环有点奇怪，我们必须按顺序选择正确的本地化方式
            for (LanguageHeader language : rc.acceptableLanguages()) {
                switch (language.tag()) {
                case "zh":
                    rc.response().putHeader("content-type", "text/plain;charset=utf-8").end("你好！");
                    return;
                case "en":
                    rc.response().end("Hello!");
                    return;
                case "fr":
                    rc.response().end("Bonjour!");
                    return;
                case "pt":
                    rc.response().end("Olá!");
                    return;
                case "es":
                    rc.response().end("Hola!");
                    return;
                }
            }
            // 我们不知道用户的语言，因此返回这个信息：
            rc.response().end("Sorry we don't speak: " + rc.preferredLanguage());
        });

        router.route(webContextPath + "/services/*").subRouter(new InternalServiceRouter().getRouter(vertx));
        router.route(webContextPath + "/publicservices/*").subRouter(new PublicServiceRouter().getRouter(vertx));
        return router;
    }

}
