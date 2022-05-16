package pers.zcc.vertxprc.launch;

import io.vertx.rxjava3.core.Vertx;
import pers.zcc.vertxprc.MainVerticle;

public class Launcher {
    static Vertx vertx;

    public static void main(String[] args) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(MainVerticle.class.getName());
    }

}
