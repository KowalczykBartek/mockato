package com.mockato.utils;

import com.mockato.model.MockDefinition;

/**
 * Because making anything on frontend is pretty annoying lets construct curl command used in example on web side.
 */
public class CurlCommandParser {
    private final static String CURL_PATTERN = "curl -X %s %s%s%s";
    private final String domain;

    public CurlCommandParser(String domain) {
        this.domain = domain;
    }

    public String generateCurlCommandForFrontend(MockDefinition definition, String subdomain) {
        return String.format(CURL_PATTERN, definition.getMethod(), subdomain, domain, definition.getPath());
    }
}
