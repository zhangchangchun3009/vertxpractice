package pers.zcc.vertxprc.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import pers.zcc.vertxprc.common.constant.Constants;
import pers.zcc.vertxprc.common.util.MongoDbUtil;
import pers.zcc.vertxprc.common.util.MysqlDbUtil;
import pers.zcc.vertxprc.common.vo.Response;
import pers.zcc.vertxprc.router.handler.LocalLanguageHandler;

public class MainRouter implements IRouterCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainRouter.class);

    private final JsonObject config;

    public MainRouter(JsonObject config) {
        this.config = config;
    }

    @Override
    public Router createRouter(Vertx vertx) {
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

        JWTAuth jwt = createJWTAuth(vertx);

        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx))).handler(ctx -> {
            ctx.put(Constants.APPLICATION_CONFIG, config);
            ctx.put(Constants.MYSQL_POOL, MysqlDbUtil.getConnectionPool());
            ctx.put(Constants.MONGODB_POOL, MongoDbUtil.getConnectionPool());
            ctx.put(Constants.JWT_AUTH, jwt);
            ctx.next();
        });

        router.route().handler(CorsHandler.create("vertx.io").allowedMethod(HttpMethod.GET));
        router.route().handler(FaviconHandler.create(vertx));

        StaticHandler staticHandler = StaticHandler.create("webroot");
        staticHandler.setIndexPage("index.html");
        router.route("/").handler(staticHandler); // any visit of not existed static resource is redirect to index.html

        router.get("/localized").handler(new LocalLanguageHandler());

        router.route(webContextPath + "/static/*").handler(staticHandler);
        router.route(webContextPath + "/dynamic/*").subRouter(new TemplateRouter().createRouter(vertx));
        router.route(webContextPath + "/services/*").subRouter(new InternalServiceRouter().createRouter(vertx));
        router.route(webContextPath + "/publicservices/*").subRouter(new PublicServiceRouter().createRouter(vertx));
        return router;
    }

    private JWTAuth createJWTAuth(Vertx vertx) {
        JWTAuthOptions jwtConfig = new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions().setPath(config.getString("auth.jwt.keystore.path"))
                        .setPassword(config.getString("auth.jwt.keystore.password"))
                        .setType(config.getString("auth.jwt.keystore.type")));
        JWTAuth jwt = JWTAuth.create(vertx, jwtConfig);
        return jwt;
    }

}
