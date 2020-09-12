package com.mockato.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Preconditions;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseContainer {
    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    public ResponseContainer(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;

        Preconditions.checkNotNull(body, "body returned from script cannot be null");
        Preconditions.checkNotNull(body, "headers returned from script cannot be null");
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
