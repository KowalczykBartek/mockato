package com.mockato.handlers;

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
    public static String responseFromException(Throwable ex) {
        String cause = ex.getCause().toString();
        return String.format(RESPONSE_FORMAT, "error", cause);
    }

    /**
     * Get response for not found case.
     *
     * @return error json message.
     */
    public static String responseForNotFoundCase() {
        return String.format(RESPONSE_FORMAT, "not_found", "resource not found.");
    }

}
