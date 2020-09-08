package com.mockato.handlers;

import com.mockato.exceptions.NotFoundException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(ResponseUtils.class);

    private final static String SOMETHING_BAD_HAPPENED_MESSAGE = "something bad happened.";
    private final static String RESPONSE_FORMAT = "{\n" + "    \"type\" : \"%s\",\n" + "    \"cause\" : \"%s\"\n" + "}";

    /**
     * Format incoming {@param ex} to json message and status code.
     *
     * @param ex
     * @return error json message.
     */
    public static void responseFromException(HttpServerResponse response, Throwable ex) {
        String cause = null;
        try {
            cause = StringEscapeUtils.escapeJava(ex.getMessage());
        } catch (Exception escapeException) {
            LOGGER.error("Unable to escape error message", escapeException.getCause());
            cause = SOMETHING_BAD_HAPPENED_MESSAGE;
        }

        if (ex instanceof IllegalStateException) {
            String message = String.format(RESPONSE_FORMAT, "error", cause);
            response.putHeader("content-type", "application/json").setStatusCode(400).end(message);
        } else if (ex instanceof NotFoundException) {
            String message = String.format(RESPONSE_FORMAT, "error", cause);
            response.putHeader("content-type", "application/json").setStatusCode(404).end(message);
        } else {
            String message = String.format(RESPONSE_FORMAT, "error", cause);
            response.putHeader("content-type", "application/json").setStatusCode(500).end(message);
        }
    }
}
