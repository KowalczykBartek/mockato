package com.mockato.handlers;

import com.mockato.model.MockDefinition;
import com.mockato.service.MockService;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler new mock creation - now upsert functionality is supported atm.
 */
public class CreateMockDefinitionHandler implements Handler<RoutingContext> {
    private static Logger LOGGER = LoggerFactory.getLogger(GetAllMocks.class);

    private final MockService mockService;

    public CreateMockDefinitionHandler(MockService mockService) {
        this.mockService = mockService;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        //FIXME some validation is required
        HttpServerResponse response = routingContext.response();
        String subdomain = routingContext.pathParam("subdomain");
        String mockId = routingContext.pathParam("mockId");

        MockDefinition definition = null;

        try {
            JsonObject jsonObject = routingContext.getBodyAsJson();
            LOGGER.info("Request to create new mock definition {} {} {}", subdomain, mockId, jsonObject);

            definition = jsonObject.mapTo(MockDefinition.class);

        } catch (Exception ex) {
            LOGGER.error("Unable to parse body - bad request for {}", routingContext.getBodyAsString());
            ResponseUtils.responseFromException(response, ex);
            return;
        }

        mockService.saveMock(subdomain, mockId, definition)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        response.end("{\"mockId\" : " + mockId + "}");
                    } else {
                        ResponseUtils.responseFromException(response, res.cause());
                    }
                });
    }
}
