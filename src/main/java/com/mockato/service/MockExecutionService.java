package com.mockato.service;

import com.mockato.exceptions.NotFoundException;
import com.mockato.exceptions.ScriptExecutionException;
import com.mockato.js.JavasScriptEngine;
import com.mockato.model.*;
import com.mockato.repo.MockRepository;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockExecutionService {
    private static Logger LOGGER = LoggerFactory.getLogger(MockExecutionService.class);

    private final MockRepository mockRepository;
    private final JavasScriptEngine javasScriptEngine;

    public MockExecutionService(MockRepository repository, JavasScriptEngine javasScriptEngine) {
        this.mockRepository = repository;
        this.javasScriptEngine = javasScriptEngine;
    }

    public Future<ResponseContainer> respondToRequestWithMock(HttpServerRequest request, String subdomain) {

        final HttpMethod method = request.method();

        LOGGER.info("Going to execute mock for subdomain {} and request {}", subdomain, request);
        return mockRepository.getAllMockedPathsForSubdomain(subdomain, method.toString())
                .compose(res -> {
                    List<MatchPattern> matchPattern = res;

                    Optional<MatchPattern> targetPattern = matchPattern.stream()
                            .filter(pattern -> {
                                Pattern compiledPattern = Pattern.compile(pattern.getPattern());
                                Matcher match = compiledPattern.matcher(request.path());
                                return match.matches();
                            })
                            .findFirst();

                    if (targetPattern.isPresent()) {
                        LOGGER.info("Found target pattern {} for request {}", targetPattern.get(), request);

                        return mockRepository.getMockDetails(subdomain, targetPattern.get().getMockId());

                    } else {
                        throw new NotFoundException("No pattern found.");
                    }


                })
                .compose(details -> {

                    LOGGER.info("Received mocked details required for executions {}", details);
                    PatternDetails patternDetails = details.getValue();
                    MockDefinition mockDefinition = details.getKey();

                    if (mockDefinition.getResponseDefinition().getType() == MockType.DYNAMIC) {

                        Map params;
                        if (patternDetails.getGroups().length > 0) {
                            Pattern compiledPattern = Pattern.compile(patternDetails.getPattern());
                            Matcher match = compiledPattern.matcher(request.path());
                            match.matches();

                            params = new HashMap();
                            for (int i = 0; i < patternDetails.getGroups().length; i++) {
                                params.put(patternDetails.getGroups()[i], match.group("p" + i));
                            }
                        } else {
                            params = new HashMap();
                        }

                        try {
                            /*
                             * In case of any exceptions from script execution, fail and return 5xx.
                             */
                            Map response = javasScriptEngine.execute(params, mockDefinition.getResponseDefinition()
                                    .getDynamicResponse().getScript());

                            final String body = Json.encode((Map) response.get("body"));
                            final int statusCode = (int) response.get("statusCode");
                            final Map<String, String> headers = (Map<String, String>) response.get("headers");

                            ResponseContainer container = new ResponseContainer(statusCode, body, headers);
                            return Future.succeededFuture(container);

                        } catch (Exception ex) {
                            return Future.failedFuture(new ScriptExecutionException(ex));
                        }

                    } else {
                        final String body = mockDefinition.getResponseDefinition().getStaticResponse().getBody();
                        final int statusCode = mockDefinition.getResponseDefinition().getStaticResponse().getStatusCode();
                        final Map<String, String> headers = mockDefinition.getResponseDefinition().getStaticResponse().getHeaders();
                        ResponseContainer container = new ResponseContainer(statusCode, body, headers);

                        return Future.succeededFuture(container);
                    }

                });


    }

}
