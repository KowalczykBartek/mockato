package com.mockato.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDefinition {
    private final MockType type;
    private final StaticResponse staticResponse;
    private final DynamicResponse dynamicResponse;

    @JsonCreator
    public ResponseDefinition(@JsonProperty("type") MockType type, @JsonProperty("staticResponse") StaticResponse staticResponse, @JsonProperty("dynamicResponse") DynamicResponse dynamicResponse) {
        this.type = type;
        this.staticResponse = staticResponse;
        this.dynamicResponse = dynamicResponse;
    }

    public MockType getType() {
        return type;
    }

    public StaticResponse getStaticResponse() {
        return staticResponse;
    }

    public DynamicResponse getDynamicResponse() {
        return dynamicResponse;
    }

    @Override
    public String toString() {
        return "ResponseDefinition{" +
                "type=" + type +
                ", staticResponse=" + staticResponse +
                ", dynamicResponse=" + dynamicResponse +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseDefinition that = (ResponseDefinition) o;
        return type == that.type &&
                Objects.equals(staticResponse, that.staticResponse) &&
                Objects.equals(dynamicResponse, that.dynamicResponse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, staticResponse, dynamicResponse);
    }
}
