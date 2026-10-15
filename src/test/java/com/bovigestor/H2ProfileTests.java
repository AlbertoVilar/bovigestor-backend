package com.bovigestor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("h2")
class H2ProfileTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldRunFarmMigrationAndCreateFarmTableAndSequence() {
        Integer successfulVersionOneMigrations = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"flyway_schema_history\" "
                        + "WHERE \"version\" = '1' AND \"success\" = TRUE",
                Integer.class
        );
        Integer farmTables = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                        + "WHERE LOWER(TABLE_SCHEMA) = 'public' AND LOWER(TABLE_NAME) = 'farm'",
                Integer.class
        );
        Integer farmSequences = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SEQUENCES "
                        + "WHERE LOWER(SEQUENCE_SCHEMA) = 'public' "
                        + "AND LOWER(SEQUENCE_NAME) = 'farm_id_seq'",
                Integer.class
        );

        assertThat(successfulVersionOneMigrations).isEqualTo(1);
        assertThat(farmTables).isEqualTo(1);
        assertThat(farmSequences).isEqualTo(1);
    }

    @Test
    void shouldAcceptUniqueCodeSeriesAndRejectDuplicates() {
        insertFarm(101L, "BVL");

        assertThatThrownBy(() -> insertFarm(102L, "BVL"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertFarm(long id, String codeSeries) {
        jdbcTemplate.update(
                "INSERT INTO farm "
                        + "(id, name, code_series, status, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                id,
                "Fazenda de teste",
                codeSeries,
                "ACTIVE"
        );
    }
}
