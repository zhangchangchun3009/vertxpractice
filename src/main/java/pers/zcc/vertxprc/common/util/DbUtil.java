package pers.zcc.vertxprc.common.util;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import pers.zcc.vertxprc.common.constant.Constants;

public class DbUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbUtil.class);

    public static MySQLPool getConnectionPool(RoutingContext routingcontext) {
        return routingcontext.get(Constants.MYSQL_POOL);
    }

    public static <U> void query(RoutingContext routingcontext, String sql, Tuple tuple, Function<Row, U> mapper,
            Handler<RowSet<U>> successHandler, Handler<Throwable> failureHandler) {
        MySQLPool db = routingcontext.get(Constants.MYSQL_POOL);
        db.getConnection(connRes -> {
            if (connRes.succeeded()) {
                SqlConnection conn = connRes.result();
                conn.preparedQuery(sql).mapping(mapper).execute(tuple, queryRes -> {
                    if (queryRes.succeeded()) {
                        successHandler.handle(queryRes.result());
                    } else {
                        failureHandler.handle(queryRes.cause());
                    }
                });
                db.close();
            } else {
                LOGGER.error("get db Connection failed,{}", connRes.cause());
                failureHandler.handle(connRes.cause());
            }
        });
    }

    public static <U> void update(RoutingContext routingcontext, String sql, Tuple tuple, Function<Row, U> mapper,
            Handler<Integer> successHandler, Handler<Throwable> failureHandler) {
        MySQLPool db = routingcontext.get(Constants.MYSQL_POOL);
        db.getConnection(connRes -> {
            if (connRes.succeeded()) {
                SqlConnection conn = connRes.result();
                conn.prepare(sql).onSuccess(ps -> {
                    Cursor cu = ps.cursor(tuple);
                    cu.read(0).onSuccess(rs -> {
                        successHandler.handle(rs.rowCount());
                        cu.close();
                    }).onFailure(failureHandler);
                }).onFailure(failureHandler);
                db.close();
            } else {
                LOGGER.error("get db Connection failed,{}", connRes.cause());
                failureHandler.handle(connRes.cause());
            }
        });
    }

}
