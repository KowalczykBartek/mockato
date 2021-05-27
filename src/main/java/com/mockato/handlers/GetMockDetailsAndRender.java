package com.mockato.handlers;

import com.mockato.model.*;
import com.mockato.service.ConfigurationService;
import com.mockato.service.MockService;
import com.mockato.utils.CurlCommandParser;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

/**
 * Get all details about mock and return HTML page.
 */
public class GetMockDetailsAndRender implements Handler<RoutingContext> {
    private final MockService mockService;
    private final ThymeleafTemplateEngine engine;
    private final CurlCommandParser curlCommandParser;
    private final String domain;

    public GetMockDetailsAndRender(ConfigurationService configurationService, MockService mockService, ThymeleafTemplateEngine engine, CurlCommandParser curlCommandParser) {
        this.mockService = mockService;
        this.engine = engine;
        this.curlCommandParser = curlCommandParser;
        this.domain = configurationService.getDomain();
    }

    @Override
    public void handle(final RoutingContext event) {

        //fixme it would make sense to validate xd
        String subdomain = event.pathParam("subdomain");
        String mockId = event.pathParam("mockId");

        mockService.getMockDetails(subdomain, mockId)
                .onComplete(result -> {
                    if (result.succeeded()) {
                        try {
                            Pair<MockDefinition, PatternDetails> details = result.result();
                            MockDefinition definition = details.getLeft();
                            PatternDetails patternDetails = details.getRight();

                            JsonObject map = new JsonObject();
                            map.put("mock_path", definition.getPath());
                            map.put("mock_method", definition.getMethod());
                            map.put("mock_id", mockId);

                            List<String> params = Arrays.asList(patternDetails.getGroups());
                            map.put("path_params", params);

                            MockType type = definition.getResponseDefinition().getType();
                            map.put("mock_type", type);

                            map.put("urlToMockato", subdomain + domain);

                            StaticResponse staticResponse = definition.getResponseDefinition().getStaticResponse();
                            map.put("mock_static_headers", staticResponse.getHeaders());
                            map.put("mock_static_status", staticResponse.getStatusCode());
                            map.put("mock_static_body", staticResponse.getBody());

                            String curlForUser = curlCommandParser.generateCurlCommandForFrontend(definition, subdomain);
                            map.put("curl_command", curlForUser);

                            engine.render(map, "webapp/html/mock_details.html", res -> {
                                if (res.succeeded()) {
                                    event.response().end(res.result());
                                } else {
                                    event.fail(res.cause());
                                }
                            });

                        } catch (Exception ex) {
                            ResponseUtils.responseFromException(event.response(), ex.getCause());
                        }

                    } else {
                        ResponseUtils.responseFromException(event.response(), result.cause());
                    }
                });
    }
}
