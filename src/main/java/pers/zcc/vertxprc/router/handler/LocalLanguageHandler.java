package pers.zcc.vertxprc.router.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.RoutingContext;

public class LocalLanguageHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext rc) {
        for (LanguageHeader language : rc.acceptableLanguages()) {
            switch (language.tag()) {
            case "zh":
                rc.response().putHeader("content-type", "text/plain;charset=utf-8").end("你好！");
                return;
            case "en":
                rc.response().end("Hello!");
                return;
            case "fr":
                rc.response().end("Bonjour!");
                return;
            case "pt":
                rc.response().end("Olá!");
                return;
            case "es":
                rc.response().end("Hola!");
                return;
            }
        }
        rc.response().end("Sorry we don't speak: " + rc.preferredLanguage());
    }

}
