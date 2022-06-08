package pers.zcc.vertxprc.router;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public interface IRouterCreator {
    Router createRouter(Vertx vertx);
}
