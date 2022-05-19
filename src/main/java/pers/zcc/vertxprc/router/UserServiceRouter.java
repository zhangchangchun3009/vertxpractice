package pers.zcc.vertxprc.router;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import pers.zcc.vertxprc.service.UserService;

public class UserServiceRouter implements IRouterCreator {

    @Override
    public Router getRouter(Vertx vertx) {
        Router router = Router.router(vertx);
        UserService userService = new UserService();
        router.get("/userService/getUser").handler(userService::getUser);
        router.post("/userService/insert").consumes("application/json").handler(userService::insert);
        return router;
    }

}
