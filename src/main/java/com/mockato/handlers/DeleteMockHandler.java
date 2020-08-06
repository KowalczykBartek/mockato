package com.mockato.handlers;

import com.mockato.service.MockService;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class DeleteMockHandler implements Handler<RoutingContext> {

    private final MockService mockService;

    public DeleteMockHandler(MockService mockService) {
        this.mockService = mockService;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        String subdomain = routingContext.pathParam("subdomain");
        String mockId = routingContext.pathParam("mockId");
        HttpServerResponse response = routingContext.response();
        mockService.deleteMock(subdomain, mockId)
                .onComplete(result -> {
                    if (result.succeeded()) {
                        response.setStatusCode(200).end();
                    } else {
                        response.setStatusCode(500).end();
                    }
                });
    }
}
