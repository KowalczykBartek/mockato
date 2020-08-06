package com.mockato.service;

import com.mockato.model.MockDefinition;
import com.mockato.model.PatternDetails;
import com.mockato.repo.MockRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockService {

    // intersection of regex chars and https://tools.ietf.org/html/rfc3986#section-3.3
    private static final Pattern RE_OPERATORS_NO_STAR = Pattern.compile("([\\(\\)\\$\\+\\.])");
    // Pattern for :<token name> in path
    private static final Pattern RE_TOKEN_SEARCH = Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)");
    private final MockRepository mockRepository;

    public MockService(MockRepository repository) {
        this.mockRepository = repository;
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
        final JsonObject asJosn = JsonObject.mapFrom(newMock);

        Pair<String, List<String>> pattern = pathToPattern(newMock.getPath());
        asJosn.put("pattern", pattern.getLeft());

        JsonArray groups = new JsonArray();
        for (String group : pattern.getRight()) {
            groups.add(group);
        }
        asJosn.put("groups", groups);

        return mockRepository.createMock(subdomain, definitionId, asJosn);
    }

    //I stole it from vert.x web FIXME add description
    public Pair pathToPattern(String path) {
        // escape path from any regex special chars
        path = RE_OPERATORS_NO_STAR.matcher(path).replaceAll("\\\\$1");

        // We need to search for any :<token name> tokens in the String and replace them with named capture groups
        Matcher m = RE_TOKEN_SEARCH.matcher(path);
        StringBuffer sb = new StringBuffer();
        List<String> groups = new ArrayList<>();
        int index = 0;
        while (m.find()) {
            String param = "p" + index;
            String group = m.group().substring(1);
            if (groups.contains(group)) {
                throw new IllegalArgumentException("Cannot use identifier " + group + " more than once in pattern string");
            }
            m.appendReplacement(sb, "(?<" + param + ">[^/]+)");
            groups.add(group);
            index++;
        }
        m.appendTail(sb);
        path = sb.toString();


        return Pair.of(path, groups);
    }
}
