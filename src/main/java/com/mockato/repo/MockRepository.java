package com.mockato.repo;

import com.mockato.exceptions.NotFoundException;
import com.mockato.model.MatchPattern;
import com.mockato.model.MockDefinition;
import com.mockato.model.PatternDetails;
import com.mockato.service.ConfigurationService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.pgclient.impl.RowImpl;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Expose API to query database. Postgres is king
 * <p>
 * yes yes i know
 * CREATE INDEX ON mocks (subdomain);
 * CREATE UNIQUE INDEX idx_uniq_sobdomain_mock_id ON mocks(subdomain, mock_id);
 * CREATE TABLE IF NOT EXISTS mocks (subdomain varchar(40), mock_id varchar(40), definition JSONB );
 */
public class MockRepository {

    private static Logger LOGGER = LoggerFactory.getLogger(MockRepository.class);

    //check how it behaves in context of threads/vert.x/vert.x's context
    private final PgPool client;

    public MockRepository(ConfigurationService configurationService) {
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setDatabase(configurationService.getDatabase())
                .setUser(configurationService.getUser())
                .setPassword(configurationService.getPassword());

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

        client = PgPool.pool(connectOptions, poolOptions);
    }

    /**
     * TODO
     *
     * @param subdomain
     * @param mockId
     * @return
     */
    public Future<Void> deleteMock(String subdomain, String mockId) {
        Promise<Void> promise = Promise.promise();
        client.preparedQuery("DELETE FROM mocks WHERE subdomain=$1 AND mock_id=$2;")
                .execute(Tuple.of(subdomain, mockId), ar -> {
                    if (ar.succeeded()) {
                        promise.complete();
                    } else {
                        LOGGER.error("Exception during query for delete mock {} {} {}", subdomain, mockId, ar.cause());
                        promise.fail(ar.cause());
                    }
                });

        return promise.future();
    }

    /**
     * TODO
     *
     * @param subdomain
     * @return
     */
    public Future<List<JsonObject>> getAllMocks(String subdomain) {

        Promise<List<JsonObject>> promise = Promise.promise();

        /*
         * TODO FIXME there is need for PAGING
         */
        client.preparedQuery("SELECT definition, mock_id from mocks WHERE subdomain=$1;")
                .execute(Tuple.of(subdomain), ar -> {
                    if (ar.succeeded()) {
                        List<JsonObject> allMocks = new ArrayList<>();

                        for (Row row : ar.result()) {
                            JsonObject definition = row.get(JsonObject.class, 0);
                            String mockId = row.getString(1);
                            definition.put("mockId", mockId);
                            allMocks.add(definition);
                        }

                        promise.complete(allMocks);
                    } else {
                        LOGGER.error("Exception during query for get all mocks for subdomain {} {}", subdomain, ar.cause());
                        promise.fail(ar.cause());
                    }
                });

        return promise.future();
    }

    /**
     * TODO
     *
     * @param subdomain
     * @param definitionId
     * @param jsonObject
     * @return
     */
    public Future<Void> createMock(String subdomain, String definitionId, JsonObject jsonObject) {
        Promise promise = Promise.promise();

        client.preparedQuery("INSERT INTO mocks(subdomain, mock_id, definition) VALUES ($1, $2, $3)")
                .execute(Tuple.of(subdomain, definitionId, jsonObject), ar -> {
                    if (ar.succeeded()) {
                        promise.complete();
                    } else {
                        LOGGER.error("Exception during mock creation {} {} {} {}", subdomain, definitionId, jsonObject, ar.cause());
                        promise.fail(ar.cause());
                    }
                });

        return promise.future();
    }

    /**
     * TODO
     *
     * @param subdomain
     * @param method
     * @return
     */
    public Future<List<MatchPattern>> getAllMockedPathsForSubdomain(String subdomain, String method) {
        Promise promise = Promise.promise();

        client.preparedQuery("SELECT mock_id, definition->>'path', definition->>'pattern', definition->>'groups' FROM mocks WHERE subdomain=$1 AND definition->>'method' = $2;")
                .execute(Tuple.of(subdomain, method), ar -> {
                    if (ar.succeeded()) {
                        List<MatchPattern> patterns = new ArrayList<>();

                        for (Row row : ar.result()) {
                            String mockId = row.getString(0);
                            String path = row.getString(1);

                            String pattern = row.getString(2);

                            //for some reason it is not parsed as an array... fixme
                            String groupsArrayString = row.getString(3);
                            String[] groups = groupsArrayString.replace("[", "").replace("]", "").split(",");

                            patterns.add(new MatchPattern(mockId, path, pattern, groups));
                        }

                        promise.complete(patterns);
                    } else {
                        LOGGER.error("Exception during query for get all paths for subdomain {} {} {}", subdomain, method, ar.cause());
                        promise.fail(ar.cause());
                    }
                });

        return promise.future();
    }

    public Future<Pair<MockDefinition, PatternDetails>> getMockDetails(String subdomain, String mockId) {
        Promise<Pair<MockDefinition, PatternDetails>> promise = Promise.promise();

        client.preparedQuery("SELECT definition FROM mocks WHERE subdomain = $1 and mock_id = $2 LIMIT 1;")
                .execute(Tuple.of(subdomain, mockId), ar -> {
                    if (ar.succeeded()) {

                        //we assume only one result or nothing returned.
                        for (Row row : ar.result()) {
                            //hello driver, wtf are you doing here ? fixme
                            JsonObject jsonObject = (JsonObject) ((RowImpl) row).getJson(0);
                            MockDefinition mockDefinition = jsonObject.mapTo(MockDefinition.class);

                            String pattern = jsonObject.getString("pattern");

                            JsonArray array = jsonObject.getJsonArray("groups");
                            String[] groups = (String[]) array.getList().<String>toArray(new String[]{});

                            promise.complete(Pair.of(mockDefinition, new PatternDetails(pattern, groups)));
                            return;// ok, lets back.
                        }

                        promise.fail(new NotFoundException(String.format("Mock for subdomain %s and mockId %s", subdomain, mockId)));
                    } else {
                        LOGGER.error("Exception during get mock details {} {} {}", subdomain, mockId, ar.cause());
                        promise.fail(ar.cause());
                    }
                });

        return promise.future();
    }
}
