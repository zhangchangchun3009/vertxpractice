package pers.zcc.vertxprc.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
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
        router.route().failureHandler(ctx -> {
            LOGGER.error("uncaught exception,", ctx.failure());
            ctx.json(new Response<String>().fail("500", "server internal error"));
        });
        Route internalRout = router.route(webContextPath + "/services/*");
        Route publicRout = router.route(webContextPath + "/publicservices/*");
        internalRout.subRouter(new InternalServiceRouter().getRouter(vertx));
        publicRout.subRouter(new PublicServiceRouter().getRouter(vertx));
        return router;
    }

}
