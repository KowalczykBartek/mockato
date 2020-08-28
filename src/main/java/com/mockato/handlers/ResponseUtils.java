package com.mockato.handlers;

import io.vertx.core.http.HttpServerResponse;

public class ResponseUtils {
    private final static String RESPONSE_FORMAT = "{\n" +
            "    \"type\" : \"%s\",\n" +
            "    \"cause\" : \"%s\"\n" +
            "}";

    /**
     * Format incoming {@param ex} to json message.
     *
     * @param ex
     * @return error json message.
     */
    public static void responseFromException(HttpServerResponse response, Throwable ex) {
        String cause = ex.getCause().toString();
        String message =  String.format(RESPONSE_FORMAT, "error", cause);

        response.putHeader("content-type", "application/json")
                .setStatusCode(404)
                .end(message);
    }

    /**
     * Get response for not found case.
     *
     */
    public static void responseForNotFoundCase(HttpServerResponse response) {
        String message = String.format(RESPONSE_FORMAT, "not_found", "resource not found.");

        response.putHeader("content-type", "application/json")
                .setStatusCode(404)
                .end(message);
    }

}
