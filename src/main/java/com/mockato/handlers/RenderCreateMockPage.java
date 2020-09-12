package com.mockato.handlers;

import com.mockato.service.ConfigurationService;
import com.mockato.service.MockService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

public class RenderCreateMockPage implements Handler<RoutingContext> {

    private final MockService mockService;
    private final ThymeleafTemplateEngine engine;
    private final String domain;

    public RenderCreateMockPage(ConfigurationService configurationService, MockService mockService, ThymeleafTemplateEngine engine) {
        this.mockService = mockService;
        this.engine = engine;
        this.domain = configurationService.getDomain();
    }

    @Override
    public void handle(RoutingContext event) {
        JsonObject map = new JsonObject();

        String subdomain = event.pathParam("subdomain");
        map.put("urlToMockato", subdomain + domain);

        engine.render(map, "webapp/html/create_mock.html", res -> {
            if (res.succeeded()) {
                event.response().end(res.result());
            } else {
                event.fail(res.cause());
            }
        });
    }
}
