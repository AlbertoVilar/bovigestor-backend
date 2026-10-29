package com.bovigestor.adapter.out.persistence;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class BovineOriginMigrationIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17")
    ).withDatabaseName("bovine_origin_migration_validation")
            .withUsername("bovine_migration_test")
            .withPassword("bovine_migration_test_password");

    @Test
    void shouldRejectV3WhenLegacyBovineRowsHaveNoOriginAndPreserveTheRow() throws Exception {
        Flyway versionTwo = flyway("2");
        versionTwo.migrate();

        try (var connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        ); var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO bovine (
                        id, internal_code, name, sex, status, registration_category,
                        created_at, updated_at
                    ) VALUES (
                        901, 'LEG-26-0901', 'Legacy bovine', 'FEMALE', 'ACTIVE', 'UNREGISTERED',
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    """);
        }

        assertThatThrownBy(() -> flyway(null).migrate())
                .isInstanceOf(FlywayException.class);

        try (var connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        ); var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM bovine WHERE id = 901")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getLong(1)).isEqualTo(1L);
        }

        assertThat(flyway("2").info().applied())
                .extracting(info -> info.getVersion().getVersion())
                .containsExactly("1", "2");
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
