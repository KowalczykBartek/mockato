package com.mockato.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DynamicResponse {
    private final String script; //todo

    @JsonCreator
    public DynamicResponse(@JsonProperty("script") String script) {
        this.script = script;
    }

    public String getScript() {
        return script;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicResponse that = (DynamicResponse) o;
        return Objects.equals(script, that.script);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script);
    }

    @Override
    public String toString() {
        return "DynamicResponse{" +
                "script='" + script + '\'' +
                '}';
    }
}
