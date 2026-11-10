package com.bovigestor.adapter.out.persistence;

import com.bovigestor.testconfig.ClassIsolatedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class BovineOwnershipPeriodMigrationTest {

    @DynamicPropertySource
    static void configureClassIsolatedPostgres(DynamicPropertyRegistry registry) {
        ClassIsolatedPostgres.register(BovineOwnershipPeriodMigrationTest.class, registry);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Test
    void shouldApplyV1ToV6AndEnforceOwnershipPeriodConstraintsOnPostgres() {
        assertThat(Arrays.stream(flyway.info().applied())
                .map(info -> info.getVersion().getVersion())
                .toList())
                .containsExactly("1", "2", "3", "4", "5", "6");
        insertFarm(1L);
        insertBovine(1L);

        long firstSequenceId = jdbcTemplate.queryForObject(
                "SELECT nextval('bovine_ownership_period_id_seq')", Long.class);
        long secondSequenceId = jdbcTemplate.queryForObject(
                "SELECT nextval('bovine_ownership_period_id_seq')", Long.class);
        assertThat(firstSequenceId).isPositive();
        assertThat(secondSequenceId).isGreaterThan(firstSequenceId);

        assertThatThrownBy(() -> insertPeriod(-1L, 1L, 1L,
                "2026-01-01 00:00:00+00", null))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertPeriod(10L, 999L, 1L,
                "2026-01-01 00:00:00+00", null))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertPeriod(11L, 1L, 999L,
                "2026-01-01 00:00:00+00", null))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertPeriod(12L, 1L, 1L,
                "2026-01-02 00:00:00+00", "2026-01-02 00:00:00+00"))
                .isInstanceOf(DataIntegrityViolationException.class);

        insertPeriod(20L, 1L, 1L,
                "2026-01-01 00:00:00+00", "2026-02-01 00:00:00+00");
        insertPeriod(21L, 1L, 1L,
                "2026-03-01 00:00:00+00", "2026-04-01 00:00:00+00");
        insertPeriod(22L, 1L, 1L,
                "2026-05-01 00:00:00+00", null);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT ended_at FROM bovine_ownership_period WHERE id = 22", Object.class))
                .isNull();
        assertThat(jdbcTemplate.update("""
                UPDATE bovine_ownership_period
                SET ended_at = TIMESTAMP WITH TIME ZONE '2026-06-01 00:00:00+00'
                WHERE id = 22
                """)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bovine_ownership_period WHERE bovine_id = 1 AND farm_id = 1",
                Long.class)).isEqualTo(3L);
    }

    private void insertFarm(long id) {
        jdbcTemplate.update("""
                INSERT INTO farm (id, name, code_series, status, created_at, updated_at)
                VALUES (?, 'Migration test farm', 'MTF', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, id);
    }

    private void insertBovine(long id) {
        jdbcTemplate.update("""
                INSERT INTO bovine (
                    id, internal_code, name, sex, status, registration_category,
                    created_at, updated_at, origin_creator_name, origin_farm_name
                ) VALUES (?, 'MIG-26-0001', 'Migration test bovine', 'FEMALE', 'ACTIVE',
                          'UNREGISTERED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
                          'Migration test creator', 'Migration test origin farm')
                """, id);
    }

    private void insertPeriod(long id, long bovineId, long farmId, String startedAt, String endedAt) {
        String endedAtSql = endedAt == null ? "NULL" : "TIMESTAMP WITH TIME ZONE '" + endedAt + "'";
        jdbcTemplate.update("""
                INSERT INTO bovine_ownership_period (id, bovine_id, farm_id, started_at, ended_at)
                VALUES (%d, %d, %d, TIMESTAMP WITH TIME ZONE '%s', %s)
                """.formatted(id, bovineId, farmId, startedAt, endedAtSql));
    }
}
