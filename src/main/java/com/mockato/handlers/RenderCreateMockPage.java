package com.mockato.handlers;

import com.mockato.service.MockService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

public class RenderCreateMockPage implements Handler<RoutingContext> {

    private final MockService mockService;
    private final ThymeleafTemplateEngine engine;

    public RenderCreateMockPage(MockService mockService, ThymeleafTemplateEngine engine) {
        this.mockService = mockService;
        this.engine = engine;
    }

    @Override
    public void handle(RoutingContext event) {
        JsonObject map = new JsonObject();

        engine.render(map, "webapp/html/create_mock.html", res -> {
            if (res.succeeded()) {
                event.response().end(res.result());
            } else {
                event.fail(res.cause());
            }
        });
    }
}
