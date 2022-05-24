package pers.zcc.vertxprc.router;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import pers.zcc.vertxprc.service.UserService;

public class UserServiceRouter implements IRouterCreator {

    @Override
    public Router getRouter(Vertx vertx) {
        JWTAuthOptions jwtConfig = new JWTAuthOptions().setKeyStore(
                new KeyStoreOptions().setPath("jwtkeystore.jceks").setPassword("zccadmin").setType("jceks"));
        JWTAuth jwt = JWTAuth.create(vertx, jwtConfig);
        Router router = Router.router(vertx);
        UserService userService = new UserService();
        router.get("/userService/getUser").handler(ctx -> {
            String token = ctx.request().getHeader("authorization");
            jwt.authenticate(new TokenCredentials(token), res -> {
                if (res.succeeded()) {
                    ctx.setUser(res.result());
                    ctx.put("jwt", jwt);
                    ctx.next();
                } else {
                    ctx.fail(403);
                }
            });
        }).handler(userService::getUser);
        router.post("/userService/insert").consumes("application/json").handler(userService::insert);
        router.get("/userService/login").handler(ctx -> {
            ctx.put("jwt", jwt);
            ctx.next();
        }).handler(userService::login);
        return router;
    }

}
