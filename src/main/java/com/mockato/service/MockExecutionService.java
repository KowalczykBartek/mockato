package com.mockato.service;

import com.mockato.codeExecution.JavasScriptEngine;
import com.mockato.exceptions.NotFoundException;
import com.mockato.exceptions.ScriptExecutionException;
import com.mockato.model.*;
import com.mockato.repo.MockRepository;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockExecutionService {
    private static Logger LOGGER = LoggerFactory.getLogger(MockExecutionService.class);

    private final MockRepository mockRepository;
    private final JavasScriptEngine javasScriptEngine;
    private final PathParserService pathParserService;

    public MockExecutionService(MockRepository repository, JavasScriptEngine javasScriptEngine, PathParserService pathParserService) {
        this.mockRepository = repository;
        this.javasScriptEngine = javasScriptEngine;
        this.pathParserService = pathParserService;
    }

    /**
     * Find mocked target for given {@param subdomain}, {@param method} and {@param path}, then execute target mock for
     * received request and return computed response (header, body and status code).
     *
     * @param subdomain
     * @param method
     * @param path
     * @return computed mocked response.
     */
    public Future<ResponseContainer> respondToRequestWithMock(String subdomain, HttpMethod method, String path) {
        LOGGER.info("Going to execute mock for subdomain {} method {} and path", subdomain, method, path);
        return mockRepository.getAllMockedPathsForSubdomain(subdomain, method.toString())
                .compose(res -> {
                    Optional<MatchPattern> targetPattern = res.stream()
                            .filter(pattern -> {
                                Pattern compiledPattern = Pattern.compile(pattern.getPattern());
                                Matcher match = compiledPattern.matcher(path);
                                return match.matches();
                            })
                            .findFirst();

                    if (targetPattern.isPresent()) {
                        LOGGER.info("Found target pattern {} for subdomain {} method {} and path", targetPattern.get(), subdomain, method, path);

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
                        Map<String, Object> params = pathParserService.extractPathParams(patternDetails, path);
                        try {
                            /*
                             * In case of any exceptions from script execution, fail and return 5xx.
                             */
                            Map response = javasScriptEngine.execute(params, mockDefinition.getResponseDefinition()
                                    .getDynamicResponse().getScript());

                            String body = Json.encode(response.get("body"));
                            int statusCode = (int) response.get("statusCode");
                            Map<String, String> headers = (Map<String, String>) response.get("headers");

                            ResponseContainer container = new ResponseContainer(statusCode, body, headers);
                            return Future.succeededFuture(container);
                        } catch (Exception ex) {
                            return Future.failedFuture(new ScriptExecutionException(ex));
                        }

                    } else {
                        String body = mockDefinition.getResponseDefinition().getStaticResponse().getBody();
                        int statusCode = mockDefinition.getResponseDefinition().getStaticResponse().getStatusCode();
                        Map<String, String> headers = mockDefinition.getResponseDefinition().getStaticResponse().getHeaders();
                        ResponseContainer container = new ResponseContainer(statusCode, body, headers);

                        return Future.succeededFuture(container);
                    }
                });
    }

}
