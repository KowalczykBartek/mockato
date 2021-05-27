package com.mockato.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDefinition {
    private final MockType type;
    private final StaticResponse staticResponse;

    @JsonCreator
    public ResponseDefinition(@JsonProperty("type") MockType type, @JsonProperty("staticResponse") StaticResponse staticResponse) {
        this.type = type;
        this.staticResponse = staticResponse;

        //only one definition type can be present
        Preconditions.checkState(staticResponse != null, "response has to be present");
    }

    public MockType getType() {
        return type;
    }

    public StaticResponse getStaticResponse() {
        return staticResponse;
    }

    @Override
    public String toString() {
        return "ResponseDefinition{" +
                "type=" + type +
                ", staticResponse=" + staticResponse +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseDefinition that = (ResponseDefinition) o;
        return type == that.type &&
                Objects.equals(staticResponse, that.staticResponse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, staticResponse);
    }
}
