package com.mockato.service;

import java.util.Map;

/**
 * Initialize (from ENV variables) and hold all configuration parameters required for application runtime.
 */
public class ConfigurationService {
    //database
    private final String user;
    private final String password;
    private final String database;

    //domain app is running behind
    private final String domain;
    private final int httpListenPort;

    public ConfigurationService(Map<String, String> map) {

        //add null check and thrown and exception
        user = map.get("DATABASE_USER");
        password = map.get("DATABASE_PASSWORD");
        database = map.get("DATABASE_NAME");

        domain = map.getOrDefault("APPLICATION_DOMAIN", ".localhost");
        httpListenPort = Integer.valueOf(map.getOrDefault("HTTP_LISTEN_PORT", "8080"));
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public String getDomain() {
        return domain;
    }

    public int getHttpListenPort() {
        return httpListenPort;
    }
}
