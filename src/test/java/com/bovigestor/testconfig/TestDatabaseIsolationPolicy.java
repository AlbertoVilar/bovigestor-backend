package com.bovigestor.testconfig;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Test-only allowlist for database endpoints owned by Testcontainers. */
public final class TestDatabaseIsolationPolicy {
    private static final String TESTCONTAINERS_JDBC_DRIVER =
            "org.testcontainers.jdbc.ContainerDatabaseDriver";
    private static final String POSTGRES_JDBC_DRIVER = "org.postgresql.Driver";
    private static final String TESTCONTAINERS_URL_PREFIX = "jdbc:tc:postgresql:17:";
    private static final Map<String, OwnedContainer> OWNED_CONTAINERS = new ConcurrentHashMap<>();

    private TestDatabaseIsolationPolicy() {
    }

    public static void registerOwnedContainer(String jdbcUrl, String username, String password) {
        OWNED_CONTAINERS.put(jdbcUrl, new OwnedContainer(username, password));
    }

    static void validate(Environment environment, boolean dataSourceConfigured) {
        String jdbcUrl = environment.getProperty("spring.datasource.url");
        if (isBlank(jdbcUrl)) {
            if (dataSourceConfigured) {
                reject("spring.datasource.url");
            }
            return;
        }

        String username = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");
        String driver = environment.getProperty("spring.datasource.driver-class-name");
        String expectedDriver;

        if (jdbcUrl.startsWith(TESTCONTAINERS_URL_PREFIX)) {
            expectedDriver = TESTCONTAINERS_JDBC_DRIVER;
        } else {
            OwnedContainer ownedContainer = OWNED_CONTAINERS.get(jdbcUrl);
            if (ownedContainer == null
                    || !equals(username, ownedContainer.username())
                    || !equals(password, ownedContainer.password())) {
                reject("spring.datasource.url");
            }
            expectedDriver = POSTGRES_JDBC_DRIVER;
        }

        requireSameIfSet(environment, "spring.datasource.driver-class-name", driver, expectedDriver);
        requireSameIfSet(environment, "spring.datasource.hikari.jdbc-url", jdbcUrl, jdbcUrl);
        requireSameIfSet(environment, "spring.datasource.hikari.username", username, username);
        requireSameIfSet(environment, "spring.datasource.hikari.password", password, password);
        requireSameIfSet(environment, "spring.flyway.url", jdbcUrl, jdbcUrl);
        requireSameIfSet(environment, "spring.flyway.user", username, username);
        requireSameIfSet(environment, "spring.flyway.password", password, password);
        requireSameIfSet(environment, "spring.flyway.driver-class-name", expectedDriver, expectedDriver);
        requireSameIfSet(environment, "spring.jpa.properties.hibernate.connection.url", jdbcUrl, jdbcUrl);
        requireSameIfSet(environment, "spring.jpa.properties.hibernate.connection.username", username, username);
        requireSameIfSet(environment, "spring.jpa.properties.hibernate.connection.password", password, password);
        requireSameIfSet(environment, "spring.jpa.properties.hibernate.connection.driver_class",
                expectedDriver, expectedDriver);

        rejectWhenSet(environment, "spring.datasource.type");
        rejectWhenSet(environment, "spring.datasource.jndi-name");
        rejectWhenSet(environment, "spring.datasource.hikari.data-source-class-name");
        rejectWhenSet(environment, "spring.datasource.xa.data-source-class-name");
        rejectWhenSet(environment, "spring.flyway.config-files");
        rejectWhenSet(environment, "spring.jpa.properties.hibernate.connection.provider_class");
        rejectWhenSet(environment, "spring.jpa.properties.hibernate.connection.datasource");
        rejectPropertyPrefix(environment, "spring.datasource.hikari.data-source-properties.");
        rejectPropertyPrefix(environment, "spring.flyway.jdbc-properties.");
    }

    private static void requireSameIfSet(Environment environment, String property, String safeValue,
                                         String expectedValue) {
        String configuredValue = environment.getProperty(property);
        if (!isBlank(configuredValue) && (!equals(configuredValue, safeValue)
                || !equals(configuredValue, expectedValue))) {
            reject(property);
        }
    }

    private static void rejectWhenSet(Environment environment, String property) {
        if (!isBlank(environment.getProperty(property))) {
            reject(property);
        }
    }

    private static void rejectPropertyPrefix(Environment environment, String prefix) {
        if (environment instanceof ConfigurableEnvironment configurableEnvironment) {
            for (PropertySource<?> source : configurableEnvironment.getPropertySources()) {
                if (source instanceof EnumerablePropertySource<?> enumerableSource) {
                    for (String property : enumerableSource.getPropertyNames()) {
                        if (property.startsWith(prefix) && !isBlank(environment.getProperty(property))) {
                            reject(prefix);
                        }
                    }
                }
            }
        }
    }

    private static boolean equals(String first, String second) {
        return first == null ? second == null : first.equals(second);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static void reject(String property) {
        throw new IllegalStateException(
                "Test database isolation rejected unsafe or conflicting property " + property
                        + " before any database connection. Use the test JDBC URL or a registered Testcontainers URL."
        );
    }

    private record OwnedContainer(String username, String password) {
    }
}
