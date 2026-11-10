package com.bovigestor.adapter.out.persistence;

import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.application.port.in.CreateBovineWithInitialOwnershipCommand;
import com.bovigestor.application.port.in.CreateBovineWithInitialOwnershipUseCase;
import com.bovigestor.domain.model.BovineOrigin;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import com.bovigestor.domain.model.FarmId;
import com.bovigestor.testconfig.TestDatabaseIsolationPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class CreateBovineWithInitialOwnershipPostgresIntegrationTest {
    private static final String INTERNAL_CODE = "PGO-26-999999";

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17")
    ).withDatabaseName("operational_creation_validation")
            .withUsername("operational_test")
            .withPassword("operational_test_password");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        TestDatabaseIsolationPolicy.registerOwnedContainer(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()
        );
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired CreateBovineWithInitialOwnershipUseCase useCase;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void shouldPersistBovineAndFirstOpenPeriodInPostgres() {
        jdbcTemplate.update("INSERT INTO farm (id, name, code_series, time_zone, status, created_at, updated_at) "
                + "VALUES (990010, 'Fazenda operacional', 'PGO', 'America/Sao_Paulo', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        var command = new CreateBovineWithInitialOwnershipCommand(
                new CreateBovineCommand(
                        "Bovina operacional", INTERNAL_CODE, BovineSex.FEMALE,
                        null, null, null, BovineStatus.ACTIVE, null,
                        null, null, BovineRegistrationCategory.UNREGISTERED,
                        null, null, null, null,
                        new BovineOrigin("Criador", "Fazenda de origem", null)
                ),
                new FarmId(990010L)
        );

        var bovineId = useCase.create(command);

        assertThat(bovineId.value()).isPositive();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bovine WHERE id = ? AND internal_code = ?",
                Integer.class, bovineId.value(), INTERNAL_CODE
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bovine_ownership_period WHERE bovine_id = ? "
                        + "AND farm_id = 990010 AND ended_at IS NULL",
                Integer.class, bovineId.value()
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT b.created_at = p.started_at FROM bovine b "
                        + "JOIN bovine_ownership_period p ON p.bovine_id = b.id WHERE b.id = ?",
                Boolean.class, bovineId.value()
        )).isTrue();
    }
}
