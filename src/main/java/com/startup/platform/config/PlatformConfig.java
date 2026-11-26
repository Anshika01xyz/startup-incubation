package com.startup.platform.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Loads defaults from application.properties and permits environment overrides. */
public record PlatformConfig(String databaseUrl, String databaseUser, String databasePassword, String datasetPath) {
    public static PlatformConfig load() {
        Properties properties = new Properties();
        try (InputStream in = PlatformConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) properties.load(in);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read application.properties", ex);
        }
        return new PlatformConfig(value("DB_URL", properties.getProperty("db.url")),
                value("DB_USER", properties.getProperty("db.user")),
                value("DB_PASSWORD", properties.getProperty("db.password")),
                value("DATASET_PATH", properties.getProperty("dataset.path", "dataset/india_startup_funding_sample.csv")));
    }
    private static String value(String env, String fallback) {
        String override = System.getenv(env);
        return override == null || override.isBlank() ? fallback : override;
    }
}
