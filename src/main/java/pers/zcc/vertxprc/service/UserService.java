package pers.zcc.vertxprc.service;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Tuple;
import pers.zcc.vertxprc.common.constant.Constants;
import pers.zcc.vertxprc.common.util.CipherUtil;
import pers.zcc.vertxprc.common.util.DbUtil;

public class UserService {

    public void getUser(RoutingContext routingcontext) {
        HttpServerRequest request = routingcontext.request();
        MultiMap queryParams = request.params();
        String name = queryParams.get("name");
        System.out.println(name);
        LocalMap<String, Object> config = routingcontext.vertx().sharedData().getLocalMap(Constants.APPLICATION_CONFIG);
        System.out.println(config.get("server.port"));
        JsonObject config2 = routingcontext.get(Constants.APPLICATION_CONFIG);
        System.out.println(config2.getString("server.port"));
        DbUtil.query(routingcontext, "select 1 as id from dual where 1=?", Tuple.of(1), row -> {
            Map<String, Object> res = new HashMap<String, Object>();
            Integer id = row.get(Integer.class, "id");
            res.put("id", id);
            return res;
        }, rowset -> {
            JsonObject re = null;
            for (Map<String, Object> row : rowset) {
                re = JsonObject.mapFrom(row);
            }
            routingcontext.json(re);
        }, error -> {
            routingcontext.fail(500, error);
        });
    }

    public void insert(RoutingContext routingcontext) {
        DbUtil.update(routingcontext, "insert into sys_sequence(seq_name,current_value,increment) values (?,?,?)",
                Tuple.of("test", 0, 1), null, cnt -> {
                    routingcontext.json(new JsonObject("{\"updated\":" + cnt + "}"));
                }, err -> {
                    routingcontext.fail(500, err);
                });

    }

    public void login(RoutingContext ctx) {
        try {
            String username = ctx.queryParam("username").get(0);
            String password = ctx.queryParam("password").get(0);
            JsonObject config = ctx.get(Constants.APPLICATION_CONFIG);
            String key = config.getString("common.user.encrypt.des.key");
            String encPass = CipherUtil.encryptByPBEWithMD5AndDES(password, key);
            DbUtil.query(ctx,
                    "select user_id as userid, user_name as username from sys_user where user_name=? and password=? limit 1",
                    Tuple.of(username, encPass), row -> {
                        int cnt = row.size();
                        if (cnt == 0) {
                            return null;
                        }
                        Map<String, Object> user = new HashMap<String, Object>();
                        user.put("username", username);
                        user.put("userid", row.getInteger("userid"));
                        return user;
                    }, rowset -> {
                        Map<String, Object> dbuser = null;
                        if (rowset.iterator().hasNext()) {
                            dbuser = rowset.iterator().next();
                        }
                        if (dbuser != null) {
                            JWTAuth jwt = ctx.get("jwt");
                            ctx.end(jwt.generateToken(
                                    new JsonObject().put("username", dbuser.get("username")).put("userid",
                                            dbuser.get("userid")),
                                    new JWTOptions().setAlgorithm("RS256").setExpiresInMinutes(30)
                                            .setIssuer("vertxprac").setNoTimestamp(false).setSubject("auth")));
                        } else {
                            ctx.fail(403);
                        }
                    }, err -> {
                        ctx.fail(500, err);
                    });
        } catch (Exception e) {
            ctx.fail(500, e);
        }
    }
}
