package pers.zcc.vertxprc.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
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
        router.errorHandler(401, ctx -> {
            ctx.json(new Response<Void>().fail("401", "not authorized"));
        }).errorHandler(403, ctx -> {
            ctx.json(new Response<Void>().fail("403", "no privillege"));
        }).errorHandler(500, ctx -> {
            LOGGER.error("uncaught exception,", ctx.failure());
            ctx.json(new Response<Void>().fail("500", "server internal error"));
        });

        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx))).handler(ctx -> {
            ctx.put(Constants.APPLICATION_CONFIG, config);
            ctx.put(Constants.MYSQL_POOL, dbPool);
            ctx.next();
        });

        router.route().handler(CorsHandler.create("vertx.io").allowedMethod(HttpMethod.GET));
        router.route().handler(FaviconHandler.create(vertx));

        StaticHandler staticHandler = StaticHandler.create("webroot");
        staticHandler.setIndexPage("index.html");
        router.route("/").handler(staticHandler); // any visit of not existed static resource is redirect to index.html

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

        router.route(webContextPath + "/static/*").handler(staticHandler);
        router.route(webContextPath + "/dynamic/*").subRouter(new TemplateRouter().getRouter(vertx));
        router.route(webContextPath + "/services/*").subRouter(new InternalServiceRouter().getRouter(vertx));
        router.route(webContextPath + "/publicservices/*").subRouter(new PublicServiceRouter().getRouter(vertx));
        return router;
    }

}
