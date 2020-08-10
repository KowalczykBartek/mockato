package com.mockato.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.http.HttpMethod;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MockDefinition {
    private final String path;
    private final HttpMethod method;
    private final ResponseDefinition responseDefinition;

    @JsonCreator
    public MockDefinition(@JsonProperty("path") String path, @JsonProperty("method") HttpMethod method, @JsonProperty("responseDefinition") ResponseDefinition responseDefinition) {
        this.path = path;
        this.method = method;
        this.responseDefinition = responseDefinition;
    }

    public String getPath() {
        return path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public ResponseDefinition getResponseDefinition() {
        return responseDefinition;
    }

    @Override
    public String toString() {
        return "MockDefinition{" +
                "path='" + path + '\'' +
                ", method=" + method +
                ", responseDefinition=" + responseDefinition +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MockDefinition that = (MockDefinition) o;
        return Objects.equals(path, that.path) &&
                Objects.equals(method, that.method) &&
                Objects.equals(responseDefinition, that.responseDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, method, responseDefinition);
    }

}
