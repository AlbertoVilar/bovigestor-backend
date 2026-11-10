package com.bovigestor.testconfig;

import com.bovigestor.BovigestorApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.datasource.AbstractDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestDatabaseIsolationConfigurationTest {
    private static final AtomicInteger DATA_SOURCE_INSTANCES = new AtomicInteger();
    private static final AtomicInteger CONNECTION_ATTEMPTS = new AtomicInteger();

    @AfterEach
    void resetCounters() {
        DATA_SOURCE_INSTANCES.set(0);
        CONNECTION_ATTEMPTS.set(0);
    }

    @Test
    void shouldRejectExternalDataSourceUrlBeforeCreatingDataSourceOrOpeningConnection() {
        assertRejectedBeforeConnection(
                "--spring.datasource.url=jdbc:postgresql://192.0.2.40:5432/fabricated",
                "--spring.datasource.driver-class-name=org.postgresql.Driver"
        );
    }

    @Test
    void shouldRejectSeparateFlywayUrlBeforeCreatingDataSourceOrOpeningConnection() {
        assertRejectedBeforeConnection(
                "--spring.flyway.url=jdbc:postgresql://192.0.2.41:5432/fabricated"
        );
    }

    @Test
    void shouldRejectPoolAndHibernateConnectionOverridesBeforeCreatingDataSource() {
        assertRejectedBeforeConnection(
                "--spring.datasource.hikari.jdbc-url=jdbc:postgresql://192.0.2.42:5432/fabricated"
        );
        assertRejectedBeforeConnection(
                "--spring.jpa.properties.hibernate.connection.url=jdbc:postgresql://192.0.2.43:5432/fabricated"
        );
    }

    private void assertRejectedBeforeConnection(String... applicationArguments) {
        DATA_SOURCE_INSTANCES.set(0);
        CONNECTION_ATTEMPTS.set(0);

        assertThatThrownBy(() -> startWithInstrumentedDataSource(applicationArguments))
                .hasStackTraceContaining("Test database isolation rejected unsafe or conflicting property");

        assertThat(DATA_SOURCE_INSTANCES).hasValue(0);
        assertThat(CONNECTION_ATTEMPTS).hasValue(0);
    }

    private ConfigurableApplicationContext startWithInstrumentedDataSource(String... applicationArguments) {
        return new SpringApplicationBuilder(BovigestorApplication.class)
                .profiles("test")
                .initializers(context -> {
                    GenericBeanDefinition definition = new GenericBeanDefinition();
                    definition.setBeanClass(InstrumentedDataSource.class);
                    ((GenericApplicationContext) context)
                            .registerBeanDefinition("dataSource", definition);
                })
                .run(applicationArguments);
    }

    public static final class InstrumentedDataSource extends AbstractDataSource {
        public InstrumentedDataSource() {
            DATA_SOURCE_INSTANCES.incrementAndGet();
        }

        @Override
        public Connection getConnection() throws SQLException {
            CONNECTION_ATTEMPTS.incrementAndGet();
            throw new SQLException("instrumented datasource must not be reached");
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return getConnection();
        }
    }
}
