package pers.zcc.vertxprc.router;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import pers.zcc.vertxprc.router.handler.JWTAuthHandler;
import pers.zcc.vertxprc.service.UserService;

public class UserServiceRouter implements IRouterCreator {

    @Override
    public Router createRouter(Vertx vertx) {
        Router router = Router.router(vertx);
        UserService userService = new UserService();
        router.get("/userService/getUser").handler(new JWTAuthHandler()).handler(userService::getUser);
        router.post("/userService/insert").consumes("application/json").handler(userService::insert);
        router.get("/userService/login").handler(userService::login);
        return router;
    }

}
