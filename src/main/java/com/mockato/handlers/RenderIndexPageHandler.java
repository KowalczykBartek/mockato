package com.mockato.handlers;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

public class RenderIndexPageHandler implements Handler<RoutingContext> {

    private final ThymeleafTemplateEngine engine;

    public RenderIndexPageHandler(ThymeleafTemplateEngine engine) {
        this.engine = engine;
    }

    @Override
    public void handle(RoutingContext event) {
        JsonObject map = new JsonObject();
        engine.render(map, "webapp/html/index.html", res -> {
            if (res.succeeded()) {
                event.response().end(res.result());
            } else {
                event.fail(res.cause());
            }
        });
    }
}
