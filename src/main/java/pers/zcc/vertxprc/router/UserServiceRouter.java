package pers.zcc.vertxprc.router;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import pers.zcc.vertxprc.common.constant.Constants;
import pers.zcc.vertxprc.service.UserService;

public class UserServiceRouter implements IRouterCreator {

    @Override
    public Router getRouter(Vertx vertx) {
        Router router = Router.router(vertx);
        UserService userService = new UserService();
        router.get("/userService/getUser").handler(ctx -> {
            String token = ctx.request().getHeader("authorization");
            JWTAuth jwt = ctx.get(Constants.JWT_AUTH);
            jwt.authenticate(new TokenCredentials(token), res -> {
                if (res.succeeded()) {
                    ctx.setUser(res.result());
                    ctx.next();
                } else {
                    ctx.fail(403);
                }
            });
        }).handler(userService::getUser);
        router.post("/userService/insert").consumes("application/json").handler(userService::insert);
        router.get("/userService/login").handler(userService::login);
        return router;
    }

}
