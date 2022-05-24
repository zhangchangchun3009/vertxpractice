package pers.zcc.vertxprc.router;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

public class TemplateRouter implements IRouterCreator {

    @Override
    public Router getRouter(Vertx vertx) {
        Router router = Router.router(vertx);
        TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);
        router.get("/Hello.ftl").handler(ctx -> {
            ctx.put("name", ctx.queryParam("name").get(0));
            ctx.put("site", "vert.x");
            ctx.next();
        });
        // put more bind data into context at this place if necessary
        router.get("/*").handler(TemplateHandler.create(engine));
        return router;
    }

}
