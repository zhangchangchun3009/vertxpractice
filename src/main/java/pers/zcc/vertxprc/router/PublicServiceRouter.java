package pers.zcc.vertxprc.router;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

public class PublicServiceRouter implements IRouterCreator {
    @Override
    public Router getRouter(Vertx vertx) {
        Router router = Router.router(vertx);
        Route rout = router.route("/*");
        rout.handler(ctx -> {
        });
        return router;
    }
}
