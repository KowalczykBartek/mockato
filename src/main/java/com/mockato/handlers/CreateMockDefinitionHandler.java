package com.mockato.handlers;

import com.mockato.model.MockDefinition;
import com.mockato.model.ResponseDefinition;
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

        try {
            JsonObject jsonObject = routingContext.getBodyAsJson();
            LOGGER.info("Request to create new mock definition {} {} {}", subdomain, mockId, jsonObject);

            MockDefinition definition = jsonObject.mapTo(MockDefinition.class);

            if (!isDefinitionValid(definition)) {
                //FIXME message
                response.setStatusCode(400).end();
                return;
            }

            mockService.saveMock(subdomain, mockId, definition)
                    .onComplete(res -> {
                        if (res.succeeded()) {
                            response.end("{\"mockId\" : " + mockId + "}");
                        } else {
                            response.setStatusCode(500).end();
                        }
                    });

        } catch (Exception ex) {
            ResponseUtils.responseFromException(response, ex.getCause());
        }
    }

    private boolean isDefinitionValid(MockDefinition definition) {
        boolean isCorrect = true;

        ResponseDefinition responseDefinition = definition.getResponseDefinition();

        if (responseDefinition.getStaticResponse() != null && responseDefinition.getDynamicResponse() != null) {
            isCorrect = false;
        }

        return isCorrect;
    }
}
