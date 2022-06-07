package pers.zcc.vertxprc.common.util;

import io.vertx.ext.mongo.MongoClient;

public class MongoDbUtil {

    private static MongoClient mongoDbPool;

    /**
     * <p>don't call this method!
     * <p>Sets the pool.
     *
     * @param mongoDbPool the new pool
     */
    public static void setPool(MongoClient mongoDbPool) {
        MongoDbUtil.mongoDbPool = mongoDbPool;
    }

    public static MongoClient getConnectionPool() {
        return mongoDbPool;
    }
}
