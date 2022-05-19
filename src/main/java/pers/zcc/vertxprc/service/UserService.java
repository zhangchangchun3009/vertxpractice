package pers.zcc.vertxprc.service;

import java.util.Map;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pers.zcc.vertxprc.verticle.MainVerticle;

public class UserService {

    private final Map<String, Object> config = MainVerticle.getApplicationConfig();

    public void getUser(RoutingContext routingcontext) {
        HttpServerRequest request = routingcontext.request();
        MultiMap queryParams = request.params();
        String name = queryParams.get("name");
        System.out.println(config.get("server.port"));
        JsonObject config2 = routingcontext.get(MainVerticle.APPLICATION_CONFIG);
        System.out.println(config2.getString("server.port"));
        routingcontext.json(name);
    }

    public void insert(RoutingContext routingcontext) {
        routingcontext.response().end("success");
    }
}
