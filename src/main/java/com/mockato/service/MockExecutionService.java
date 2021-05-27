package com.mockato.service;

import com.mockato.exceptions.NotFoundException;
import com.mockato.model.MatchPattern;
import com.mockato.model.MockDefinition;
import com.mockato.model.PatternDetails;
import com.mockato.model.ResponseContainer;
import com.mockato.repo.MockRepository;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.http.HttpMethod;
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
    private final PathParserService pathParserService;
    private final WorkerExecutor workerExecutor;

    public MockExecutionService(Vertx vertx, MockRepository repository, PathParserService pathParserService) {
        this.mockRepository = repository;
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
        LOGGER.info("Going to execute mock for subdomain {} method {} and path {}", subdomain, method, path);
        return mockRepository.getAllMockedPathsForSubdomain(subdomain, method.toString())
                .compose(res -> {
                    Optional<MatchPattern> targetPattern = res.stream()
                            .filter(pattern -> {
                                //maybe we can cache that, now its not important
                                Pattern compiledPattern = Pattern.compile(pattern.getPattern());
                                Matcher match = compiledPattern.matcher(path);
                                return match.matches();
                            })
                            .findFirst();

                    if (targetPattern.isPresent()) {
                        LOGGER.info("Found target pattern {} for subdomain {} method {} and path {}", targetPattern.get(), subdomain, method, path);

                        return mockRepository.getMockDetails(subdomain, targetPattern.get().getMockId());

                    } else {
                        throw new NotFoundException("No pattern found.");
                    }
                })
                .map(details -> executeMock(details, path));
    }

    private ResponseContainer executeMock(Pair<MockDefinition, PatternDetails> details, String path) {
        LOGGER.info("Received mocked details required for executions {}", details);
        PatternDetails patternDetails = details.getValue();
        MockDefinition mockDefinition = details.getKey();

        String body = mockDefinition.getResponseDefinition().getStaticResponse().getBody();
        int statusCode = mockDefinition.getResponseDefinition().getStaticResponse().getStatusCode();
        Map<String, String> headers = mockDefinition.getResponseDefinition().getStaticResponse().getHeaders();
        return new ResponseContainer(statusCode, body, headers);
    }

}
