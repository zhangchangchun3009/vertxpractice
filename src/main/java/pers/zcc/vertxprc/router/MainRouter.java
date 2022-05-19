package pers.zcc.vertxprc.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import pers.zcc.vertxprc.verticle.MainVerticle;
import pers.zcc.vertxprc.vo.Response;

public class MainRouter implements IRouterCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainRouter.class);

    private final JsonObject config;

    public MainRouter(JsonObject config) {
        this.config = config;
    }

    @Override
    public Router getRouter(Vertx vertx) {
        String webContextPath = config.getString("server.contextPath");
        Router router = Router.router(vertx);
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx))).handler(ctx -> {
            ctx.put(MainVerticle.APPLICATION_CONFIG, config);
            ctx.next();
        }).failureHandler(ctx -> {
            LOGGER.error("uncaught exception,", ctx.failure());
            ctx.json(new Response<String>().fail("500", "server internal error"));
        });

        router.get("/localized").handler(rc -> {
            //虽然通过一个 switch 循环有点奇怪，我们必须按顺序选择正确的本地化方式
            for (LanguageHeader language : rc.acceptableLanguages()) {
                switch (language.tag()) {
                case "zh":
                    rc.response().end("你好！");
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
