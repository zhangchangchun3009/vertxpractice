package pers.zcc.vertxprc;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.NetServerOptions;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    HttpServer httpServer;

    int port = 8888;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        HttpServerOptions options = new HttpServerOptions();
        options.setPort(port);
        httpServer = vertx.createHttpServer(options);
        httpServer.requestHandler(req -> {
            LOGGER.info("uri:{}", req.absoluteURI());
            LOGGER.info("method:{}", req.method().name());
            LOGGER.info("param:{}", req.getParam("a"));
            req.response().putHeader("content-type", "text/html").end("Hello from Vert.x!");
        }).listen(port, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                LOGGER.info("HTTP server started on port 8888");
            } else {
                startPromise.fail(http.cause());
            }
        });
        stopVertx(startPromise);
    }

    private void stopVertx(Promise<Void> startPromise) {
        vertx.createNetServer(new NetServerOptions().setWriteIdleTimeout(50)).connectHandler(socket -> {
            if (socket.remoteAddress().host() != null
                    && socket.remoteAddress().host().equals(socket.localAddress().host())) {
                socket.handler(buffer -> {
                    String cmd = new String(buffer.getBytes(), Charset.forName("utf-8"));
                    if ("SHUTDOWNXX".equals(cmd)) {
                        httpServer.close().onSuccess(handler -> {
                            LOGGER.info("HTTP server stop success");
                        }).onFailure(h -> {
                            LOGGER.info("HTTP server stop failed");
                        });
                        try {
                            stop();
                        } catch (Exception e) {
                            LOGGER.info("vert.x stop failed,", e);
                        }
                    }
                });
            }
        }).listen(8885, server -> {
            if (server.succeeded()) {
                LOGGER.info("HTTP server is listening shutdown command on port 8885");
            } else {
                LOGGER.error("HTTP server is listening shutdown service started failed");
                startPromise.fail(server.cause());
            }
        });

    }

    @Override
    public void stop() throws Exception {
        vertx.close(res -> {
            if (res.succeeded()) {
                System.exit(1);
            } else {
                LOGGER.info("vertx stop failed");
            }
        });
    }
}
