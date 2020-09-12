package com.mockato.service;

import com.mockato.codeExecution.JavasScriptEngine;
import com.mockato.exceptions.NotFoundException;
import com.mockato.exceptions.ScriptExecutionException;
import com.mockato.model.*;
import com.mockato.repo.MockRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import org.apache.commons.lang3.tuple.Pair;
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
    private final WorkerExecutor workerExecutor;

    public MockExecutionService(Vertx vertx, MockRepository repository, JavasScriptEngine javasScriptEngine, PathParserService pathParserService) {
        this.mockRepository = repository;
        this.javasScriptEngine = javasScriptEngine;
        this.pathParserService = pathParserService;

        workerExecutor = vertx.createSharedWorkerExecutor("javascript-execution-engine-pool", 1, 10000000000l * 3);
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
                .compose(details -> executeMock(details, path));
    }

    private Future<ResponseContainer> executeMock(Pair<MockDefinition, PatternDetails> details, String path) {
        LOGGER.info("Received mocked details required for executions {}", details);
        PatternDetails patternDetails = details.getValue();
        MockDefinition mockDefinition = details.getKey();

        if (mockDefinition.getResponseDefinition().getType() == MockType.DYNAMIC) {
            Map<String, Object> params = pathParserService.extractPathParams(patternDetails, path);
            return executeJavaScriptEngine(mockDefinition, params);

        } else {
            String body = mockDefinition.getResponseDefinition().getStaticResponse().getBody();
            int statusCode = mockDefinition.getResponseDefinition().getStaticResponse().getStatusCode();
            Map<String, String> headers = mockDefinition.getResponseDefinition().getStaticResponse().getHeaders();
            ResponseContainer container = new ResponseContainer(statusCode, body, headers);

            return Future.succeededFuture(container);
        }
    }

    /*
     * we should not block main loop.
     * TODO we should implement some queue before, because there is no such for vert.x's thread pool.
     */
    private Future<ResponseContainer> executeJavaScriptEngine(MockDefinition mockDefinition, Map<String, Object> params) {
        Promise<ResponseContainer> responseContainerPromise = Promise.promise();

        //TODO - cool idea, when we detect script that is bigger than limits assume, lets mark it as
        //TODO malicious and display on frontend
        workerExecutor.<ResponseContainer>executeBlocking(promise -> {
            try {
                Map response = javasScriptEngine.execute(params, mockDefinition.getResponseDefinition()
                        .getDynamicResponse().getScript());

                String body = Json.encode(response.get("body"));
                int statusCode = (int) response.get("statusCode");
                Map<String, String> headers = (Map<String, String>) response.get("headers");
                ResponseContainer container = new ResponseContainer(statusCode, body, headers);

                if (isResponseValid(container)) {
                    promise.complete(container);
                } else {
                    promise.fail(new ScriptExecutionException("Response returned from the script is not valid"));
                }

            } catch (Exception ex) {
                throw new ScriptExecutionException(ex);
            }

        }, false, res -> {
            if (res.failed()) {
                responseContainerPromise.fail(res.cause());
            } else {
                responseContainerPromise.complete(res.result());
            }
        });

        return responseContainerPromise.future();
    }

    /*
     * Before returning it to the user, lets verify we can return it safely.
     */
    private boolean isResponseValid(ResponseContainer responseContainer) {
        boolean isAtLeastOneNullHeader = responseContainer.getHeaders().entrySet().stream() //
                .anyMatch(header -> header.getValue() == null || header.getKey() == null);

        //according to https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
        boolean isStatusValid = responseContainer.getStatusCode() >= 100 && responseContainer.getStatusCode() < 600;

        return !isAtLeastOneNullHeader && isStatusValid;
    }

}
