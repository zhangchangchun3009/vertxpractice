package pers.zcc.vertxprc.verticle;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import pers.zcc.vertxprc.common.constant.Constants;
import pers.zcc.vertxprc.router.MainRouter;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private static MySQLPool dbPool;

    public static MySQLPool getDbPool() {
        return dbPool;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        ConfigStoreOptions configStoreOptions = new ConfigStoreOptions().setType("file").setFormat("properties")
                .setOptional(false).setConfig(new JsonObject().put("path", "application.properties"));
        ConfigRetrieverOptions configRetrieverOpts = new ConfigRetrieverOptions().addStore(configStoreOptions);
        ConfigRetriever configRetriever = ConfigRetriever.create(vertx, configRetrieverOpts);
        configRetriever.getConfig(res -> {
            if (res.succeeded()) {
                JsonObject jsonObject = res.result();
                JsonObject config = config();
                config.mergeIn(jsonObject, true);
                SharedData sd = vertx.sharedData();
                LocalMap<String, Object> applicationConfig = sd.getLocalMap(Constants.APPLICATION_CONFIG);
                applicationConfig.putAll(config.getMap());

                dbPool = createDbConn(jsonObject);

                HttpServer httpServer = createWebServer(startPromise, config, dbPool);

                listenOnShutdownSocket(httpServer, config, startPromise);
            } else {
                LOGGER.error("application configuration is not properly configed");
            }
        });

    }

    private MySQLPool createDbConn(JsonObject jsonObject) {
        PoolOptions dbPoolConfig = new PoolOptions();
        dbPoolConfig.setPoolCleanerPeriod(jsonObject.getInteger("db.mysql.pool.cleanerPeriod"));//连接压力大时缩减
        dbPoolConfig.setMaxSize(jsonObject.getInteger("db.mysql.pool.maxSize"));//最大连接数
        dbPoolConfig.setMaxWaitQueueSize(jsonObject.getInteger("db.mysql.pool.maxWaitQueueSize"));
        dbPoolConfig.setName("mysqlDB");
        dbPoolConfig.setShared(true);
        String connectionUri = jsonObject.getString("db.mysql.uri");
        MySQLConnectOptions connectOptions = MySQLConnectOptions.fromUri(connectionUri);
        connectOptions.setUser(jsonObject.getString("db.mysql.username"));
        connectOptions.setPassword(jsonObject.getString("db.mysql.password"));
        return MySQLPool.pool(vertx, connectOptions, dbPoolConfig);
    }

    private HttpServer createWebServer(Promise<Void> startPromise, JsonObject config, MySQLPool dbPool) {
        Router mainRouter = new MainRouter(config, dbPool).getRouter(vertx);
        int port = config.getInteger("server.port");
        HttpServerOptions options = new HttpServerOptions();
        options.setPort(port);
        options.setHandle100ContinueAutomatically(true);
        options.setWriteIdleTimeout(config.getInteger("server.writeIdleTimeout")); //socket写超时（接口响应超时）
        options.setIdleTimeout(config.getInteger("server.idleTimeout"));//关闭keepalive连接的时间，默认是不会关闭的
        HttpServer httpServer = vertx.createHttpServer(options);
        httpServer.requestHandler(mainRouter).exceptionHandler(e -> {
            LOGGER.error("socket exception logged,", e);
        }).webSocketHandler(websocket -> {
            LOGGER.info(websocket.path());
            websocket.writeTextMessage("hellow");
        }).listen(port, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                LOGGER.info("HTTP server started on port: {}, context path: {}", port,
                        config.getString("server.contextPath"));
            } else {
                startPromise.fail(http.cause());
            }
        });
        return httpServer;
    }

    private void listenOnShutdownSocket(HttpServer httpServer, JsonObject config, Promise<Void> startPromise) {
        vertx.createNetServer(
                new NetServerOptions().setWriteIdleTimeout(config.getInteger("server.shutdown.writeIdleTimeout")))
                .connectHandler(socket -> {
                    if (socket.remoteAddress().host() != null
                            && socket.remoteAddress().host().equals(socket.localAddress().host())) {
                        socket.handler(buffer -> {
                            String cmd = new String(buffer.getBytes(), Charset.forName("utf-8"));
                            if (config.getString("server.shutdown.command").equals(cmd)) {
                                httpServer.close().onSuccess(handler -> {
                                    LOGGER.info("HTTP server stop success");
                                }).onFailure(h -> {
                                    LOGGER.info("HTTP server stop failed");
                                });
                                try {
                                    vertx.close(res -> {
                                        if (res.succeeded()) {
                                            LOGGER.info("system exit");
                                        } else {
                                            LOGGER.info("vertx stop failed");
                                        }
                                    });
                                } catch (Exception e) {
                                    LOGGER.info("vert.x stop failed,", e);
                                }
                            }
                        });
                    }
                }).listen(config.getInteger("server.shutdown.listenPort"), server -> {
                    if (server.succeeded()) {
                        LOGGER.info("HTTP server is listening shutdown command on port: {}",
                                config.getInteger("server.shutdown.listenPort"));
                    } else {
                        LOGGER.error("HTTP server is listening shutdown service started failed");
                        startPromise.fail(server.cause());
                    }
                });
    }

    @Override
    public void stop() throws Exception {

    }

}
