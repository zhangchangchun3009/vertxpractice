package pers.zcc.vertxprc.launch;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import pers.zcc.vertxprc.verticle.MainVerticle;

public class Launcher {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions commonDeployOpts = new DeploymentOptions();
        commonDeployOpts.setInstances(Runtime.getRuntime().availableProcessors() * 2 - 1);
        vertx.deployVerticle(MainVerticle.class.getName(), commonDeployOpts);
    }

}
