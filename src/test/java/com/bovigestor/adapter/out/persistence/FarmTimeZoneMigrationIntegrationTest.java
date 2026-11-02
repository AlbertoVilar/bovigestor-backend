package com.bovigestor.adapter.out.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class FarmTimeZoneMigrationIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17")
    ).withDatabaseName("farm_time_zone_migration_validation")
            .withUsername("farm_time_zone_test")
            .withPassword("farm_time_zone_test_password");

    @Test
    void shouldPreserveExistingFarmWithoutInventingTimeZone() throws Exception {
        flyway("3").migrate();

        try (var connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        ); var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO farm (id, name, code_series, status, created_at, updated_at)
                    VALUES (501, 'Legacy farm', 'LGF', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """);
        }

        flyway(null).migrate();

        try (var connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        ); var statement = connection.createStatement();
             var result = statement.executeQuery(
                     "SELECT id, time_zone FROM farm WHERE id = 501")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getLong("id")).isEqualTo(501L);
            assertThat(result.getString("time_zone")).isNull();
        }

        assertThat(flyway(null).info().applied())
                .extracting(info -> info.getVersion().getVersion())
                .containsExactly("1", "2", "3", "4");
    }

    private static Flyway flyway(String target) {
        var configuration = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration");
        if (target != null) {
            configuration.target(target);
        }
        return configuration.load();
    }
}
