package pers.zcc.vertxprc.router.handler;

import io.vertx.core.Handler;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import pers.zcc.vertxprc.common.constant.Constants;

/**
 * The Class JWTAuthHandler.use it wherever a router needs jwt auth.
 * to get a jwt token, call the login service
 * handle http request which uses a jwt token that is passed by the http header "authorization".
 * return a http code 403 if auth is failed, else decrypt user and set it to the user field
 *        in the RoutingContext and continue
 * @author zhangchangchun
 * @Date 2022年6月8日
 */
public class JWTAuthHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
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
    }

}
