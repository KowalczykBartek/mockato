package com.mockato.service;

import com.mockato.model.MockDefinition;
import com.mockato.model.PatternDetails;
import com.mockato.repo.MockRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class MockService {
    private final MockRepository mockRepository;
    private final PathParserService pathParserService;

    public MockService(MockRepository repository, PathParserService pathParserService) {
        this.mockRepository = repository;
        this.pathParserService = pathParserService;
    }

    public Future<Pair<MockDefinition, PatternDetails>> getMockDetails(String subdomain, String mockId) {
        return mockRepository.getMockDetails(subdomain, mockId);
    }

    public Future<Void> deleteMock(String subdomain, String mockId) {
        return mockRepository.deleteMock(subdomain, mockId);
    }

    public Future<List<JsonObject>> getAllMocks(String subdomain) {
        return mockRepository.getAllMocks(subdomain);
    }

    public Future<Void> saveMock(String subdomain, String definitionId, MockDefinition newMock) {
        JsonObject asJosn = JsonObject.mapFrom(newMock);

        Pair<String, List<String>> pattern = pathParserService.pathToPattern(newMock.getPath());
        asJosn.put("pattern", pattern.getLeft());

        JsonArray groups = new JsonArray();
        for (String group : pattern.getRight()) {
            groups.add(group);
        }
        asJosn.put("groups", groups);

        return mockRepository.createMock(subdomain, definitionId, asJosn);
    }
}
