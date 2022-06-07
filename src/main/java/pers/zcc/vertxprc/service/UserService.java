package pers.zcc.vertxprc.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import pers.zcc.vertxprc.common.util.MongoDbUtil;
import pers.zcc.vertxprc.common.util.MysqlDbUtil;

public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public void getUser(RoutingContext routingcontext) {
        HttpServerRequest request = routingcontext.request();
        MultiMap queryParams = request.params();
        String name = queryParams.get("name");
        System.out.println(name);
        LocalMap<String, Object> config = routingcontext.vertx().sharedData().getLocalMap(Constants.APPLICATION_CONFIG);
        LOGGER.info(config.get("server.port").toString());
        JsonObject config2 = routingcontext.get(Constants.APPLICATION_CONFIG);
        LOGGER.info(config2.getString("server.port"));
        MysqlDbUtil.query(routingcontext, "select 1 as id from dual where 1=?", Tuple.of(1), row -> {
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
        MysqlDbUtil.update(routingcontext, "insert into sys_sequence(seq_name,current_value,increment) values (?,?,?)",
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
            MysqlDbUtil.query(ctx,
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
                            String username2 = String.valueOf(dbuser.get("username"));
                            String userid2 = String.valueOf(dbuser.get("userid"));
                            JWTAuth jwt = ctx.get(Constants.JWT_AUTH);
                            String token = jwt.generateToken(
                                    new JsonObject().put("username", username2).put("userid", userid2),
                                    new JWTOptions().setAlgorithm("RS256").setExpiresInMinutes(30)
                                            .setIssuer("vertxprac").setNoTimestamp(false).setSubject("auth"));
                            MongoDbUtil.getConnectionPool()
                                    .insert("sys.user",
                                            new JsonObject().put("userid", userid2).put("username", username2)
                                                    .put("token", token).put("time", System.currentTimeMillis()),
                                            insertRes -> {
                                                if (insertRes.succeeded()) {
                                                    LOGGER.debug(
                                                            "insert login user: {} info into mongo success.data id: {}",
                                                            username2, insertRes.result());
                                                } else {
                                                    LOGGER.error("insert login user: {} info into mongo failed.",
                                                            username2);
                                                }
                                            });
                            ctx.end(token);
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
