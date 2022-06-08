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
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import pers.zcc.vertxprc.common.constant.Constants;
import pers.zcc.vertxprc.common.util.MongoDbUtil;
import pers.zcc.vertxprc.common.util.MysqlDbUtil;
import pers.zcc.vertxprc.router.MainRouter;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

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

                MySQLPool mysqlDbPool = createMysqlDbConn(jsonObject);
                MysqlDbUtil.setPool(mysqlDbPool);

                MongoClient mongoDbPool = createMongoDbConn(jsonObject);
                MongoDbUtil.setPool(mongoDbPool);

                HttpServer httpServer = createWebServer(startPromise, config);

                listenOnShutdownSocket(httpServer, config, startPromise);
            } else {
                LOGGER.error("application configuration is not properly configed");
            }
        });

    }

    private MongoClient createMongoDbConn(JsonObject jsonObject) {
        String connectionString = jsonObject.getString("db.mongodb.uri");
        MongoClient mongoClient = MongoClient.createShared(vertx,
                new JsonObject().put("useObjectId", jsonObject.getBoolean("db.mongodb.useObjectId", false))
                        .put("db_name", jsonObject.getString("db.mongodb.defaultDB", "test"))
                        .put("connection_string", connectionString),
                "mongoDB");
        return mongoClient;
    }

    private MySQLPool createMysqlDbConn(JsonObject jsonObject) {
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

    private HttpServer createWebServer(Promise<Void> startPromise, JsonObject config) {
        int httpsPort = config.getInteger("server.https.port");
        HttpServerOptions options = new HttpServerOptions();
        options.setPort(httpsPort);
        options.setTcpKeepAlive(true);
        options.setSsl(true);
        JksOptions keystoreOptions = new JksOptions();
        keystoreOptions.setPath(config.getString("server.https.keystore.path"))
                .setPassword(config.getString("server.https.keystore.password"))
                .setAlias(config.getString("server.https.keystore.alias"))
                .setAliasPassword(config.getString("server.https.keystore.aliaspassword"));
        options.setKeyStoreOptions(keystoreOptions);
        options.setHandle100ContinueAutomatically(true);
        options.setWriteIdleTimeout(config.getInteger("server.writeIdleTimeout")); //socket写超时（接口响应超时）
        options.setIdleTimeout(config.getInteger("server.idleTimeout"));//关闭keepalive连接的时间，默认是不会关闭的
        HttpServer httpsServer = vertx.createHttpServer(options);
        httpsServer.requestHandler(new MainRouter(config).createRouter(vertx)).exceptionHandler(e -> {
            LOGGER.error("socket exception logged,", e);
        }).webSocketHandler(websocket -> {
            LOGGER.info(websocket.path());
            websocket.writeTextMessage("hellow");
        }).listen(httpsPort, https -> {
            if (https.succeeded()) {
                int httpPort = config.getInteger("server.http.port");
                vertx.createHttpServer().requestHandler(req -> {
                    req.response().putHeader("location",
                            "https://" + req.host().substring(0, req.host().indexOf(":")) + ":" + httpsPort + req.uri())
                            .setStatusCode(302).end();
                }).listen(httpPort, http -> {
                    LOGGER.info(
                            "HTTP server started on port: {}, context path: {}. Any request of http connection will be redirected to https connection",
                            httpPort, config.getString("server.contextPath"));
                });
                startPromise.complete();
                LOGGER.info("HTTPS server started on port: {}, context path: {}", httpsPort,
                        config.getString("server.contextPath"));
            } else {
                startPromise.fail(https.cause());
            }
        });
        return httpsServer;
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
