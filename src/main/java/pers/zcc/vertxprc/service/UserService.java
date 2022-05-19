package pers.zcc.vertxprc.service;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class UserService {

    public void getUser(RoutingContext routingcontext) {
        HttpServerRequest request = routingcontext.request();
        MultiMap queryParams = request.params();
        String name = queryParams.get("name");
        routingcontext.json(name);
    }

    public void insert(RoutingContext routingcontext) {
        routingcontext.response().end("success");
    }
}
