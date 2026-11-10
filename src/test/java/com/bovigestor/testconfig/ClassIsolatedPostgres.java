package com.bovigestor.testconfig;

import org.springframework.test.context.DynamicPropertyRegistry;

/** Registers a disposable PostgreSQL database dedicated to one Spring test class. */
public final class ClassIsolatedPostgres {

    private ClassIsolatedPostgres() {
    }

    public static void register(Class<?> testClass, DynamicPropertyRegistry registry) {
        String databaseName = "bovigestor_" + testClass.getSimpleName()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .replaceAll("[^A-Za-z0-9_]", "_")
                .toLowerCase(java.util.Locale.ROOT);
        registry.add("spring.datasource.url",
                () -> "jdbc:tc:postgresql:17:///" + databaseName);
        registry.add("spring.datasource.username", () -> "test");
        registry.add("spring.datasource.password", () -> "test");
        registry.add("spring.datasource.driver-class-name",
                () -> "org.testcontainers.jdbc.ContainerDatabaseDriver");
    }
}
