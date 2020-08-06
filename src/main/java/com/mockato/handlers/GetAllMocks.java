package com.mockato.handlers;

import com.mockato.service.MockService;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Handler request to return all mocks for subdomain.
 * FIXME no paging at the moment.
 */
public class GetAllMocks implements Handler<RoutingContext> {

    private static Logger LOGGER = LoggerFactory.getLogger(GetAllMocks.class);

    private final MockService mockService;

    public GetAllMocks(MockService mockService) {
        this.mockService = mockService;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        String subdomain = routingContext.pathParam("subdomain");
        LOGGER.info("Get all mocks for subdomain {}", subdomain);
        HttpServerResponse response = routingContext.response();

        mockService.getAllMocks(subdomain)
                .onComplete(result -> {
                    if (result.succeeded()) {
                        try {
                            List<JsonObject> allMocks = result.result();
                            String encodedResponse = Json.encode(allMocks);
                            response.setStatusCode(200).end(encodedResponse);
                        } catch (EncodeException ex) {
                            response.setStatusCode(500).end(ResponseUtils.responseFromException(result.cause()));
                        }
                    } else {
                        response.setStatusCode(500).end(ResponseUtils.responseFromException(result.cause()));
                    }
                });
    }
}
