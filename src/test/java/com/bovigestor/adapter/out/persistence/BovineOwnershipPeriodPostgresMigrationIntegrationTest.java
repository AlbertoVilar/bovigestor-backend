package com.bovigestor.adapter.out.persistence;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@Testcontainers
class BovineOwnershipPeriodPostgresMigrationIntegrationTest {
    private static final String OPEN_PERIOD_INDEX = "uq_bovine_one_open_ownership";
    private static final AtomicInteger SCHEMA_SEQUENCE = new AtomicInteger();

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17")
    ).withDatabaseName("ownership_period_migration_validation")
            .withUsername("ownership_period_test")
            .withPassword("ownership_period_test_password");

    @Test
    void shouldApplyV1ToV6AndEnforceOwnershipPeriodConstraintsOnPostgres() throws Exception {
        String schema = newSchemaName();
        Flyway flyway = migrate(schema, null);

        try (Connection connection = openConnection(schema)) {
            assertThat(appliedVersions(flyway))
                    .containsExactly("1", "2", "3", "4", "5", "6");
            assertThat(indexExists(connection, schema, OPEN_PERIOD_INDEX)).isTrue();

            insertFarm(connection, 1L);
            insertFarm(connection, 2L);
            insertBovine(connection, 1L);
            insertBovine(connection, 2L);

            long firstSequenceId = nextSequenceId(connection);
            long secondSequenceId = nextSequenceId(connection);
            assertThat(firstSequenceId).isPositive();
            assertThat(secondSequenceId).isGreaterThan(firstSequenceId);

            assertPostgresViolation(() -> insertPeriod(connection, -1L, 1L, 1L,
                    "2026-01-01 00:00:00+00", null), "23514", "ck_bovine_ownership_period_id_positive");
            assertPostgresViolation(() -> insertPeriod(connection, 10L, 999L, 1L,
                    "2026-01-01 00:00:00+00", null), "23503", "fk_bovine_ownership_period_bovine");
            assertPostgresViolation(() -> insertPeriod(connection, 11L, 1L, 999L,
                    "2026-01-01 00:00:00+00", null), "23503", "fk_bovine_ownership_period_farm");
            assertPostgresViolation(() -> insertPeriod(connection, 12L, 1L, 1L,
                    "2026-01-02 00:00:00+00", "2026-01-02 00:00:00+00"),
                    "23514", "ck_bovine_ownership_period_end_after_start");

            insertPeriod(connection, 20L, 1L, 1L,
                    "2026-01-01 00:00:00+00", "2026-02-01 00:00:00+00");
            insertPeriod(connection, 21L, 1L, 1L,
                    "2026-03-01 00:00:00+00", "2026-04-01 00:00:00+00");
            insertPeriod(connection, 22L, 1L, 1L,
                    "2026-05-01 00:00:00+00", null);

            assertPostgresViolation(() -> insertPeriod(connection, 20L, 1L, 1L,
                    "2026-06-01 00:00:00+00", "2026-07-01 00:00:00+00"),
                    "23505", "pk_bovine_ownership_period");

            assertThat(openPeriodCount(connection, 1L)).isEqualTo(1L);
            assertThat(periodCount(connection, 1L)).isEqualTo(3L);
            assertPostgresViolation(() -> insertPeriod(connection, 23L, 1L, 2L,
                    "2026-06-01 00:00:00+00", null), "23505", OPEN_PERIOD_INDEX);
            assertThat(periodCount(connection, 1L))
                    .as("the rejected second open period must not be persisted")
                    .isEqualTo(3L);

            insertPeriod(connection, 24L, 2L, 2L,
                    "2026-06-01 00:00:00+00", null);
            assertThat(openPeriodCount(connection, 2L)).isEqualTo(1L);

            try (var statement = connection.createStatement()) {
                assertThat(statement.executeUpdate("""
                        UPDATE bovine_ownership_period
                        SET ended_at = TIMESTAMP WITH TIME ZONE '2026-06-01 00:00:00+00'
                        WHERE id = 22
                        """)).isEqualTo(1);
            }
            insertPeriod(connection, 25L, 1L, 2L,
                    "2026-06-01 00:00:00+00", null);

            assertThat(openPeriodCount(connection, 1L)).isEqualTo(1L);
            assertThat(periodCount(connection, 1L)).isEqualTo(4L);
            assertThat(periodEnd(connection, 22L)).isEqualTo(Instant.parse("2026-06-01T00:00:00Z"));
            assertThat(periodEnd(connection, 20L)).isEqualTo(Instant.parse("2026-02-01T00:00:00Z"));
            assertThat(periodEnd(connection, 21L)).isEqualTo(Instant.parse("2026-04-01T00:00:00Z"));
            assertThat(periodEnd(connection, 25L)).isNull();
        }
    }

    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    void shouldAllowOnlyOneConcurrentOpenPeriodForTheSameBovine() throws Exception {
        String schema = newSchemaName();
        migrate(schema, null);
        try (Connection setup = openConnection(schema)) {
            insertFarm(setup, 1L);
            insertBovine(setup, 1L);
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch contenderReady = new CountDownLatch(1);
        AtomicInteger contenderPid = new AtomicInteger();
        try (Connection firstTransaction = openConnection(schema)) {
            firstTransaction.setAutoCommit(false);
            setStatementTimeout(firstTransaction);
            insertPeriod(firstTransaction, 40L, 1L, 1L,
                    "2026-01-01 00:00:00+00", null);

            Future<ConcurrentInsertOutcome> contender = executor.submit(() -> {
                try (Connection connection = openConnection(schema)) {
                    connection.setAutoCommit(false);
                    setStatementTimeout(connection);
                    contenderPid.set(backendPid(connection));
                    contenderReady.countDown();
                    try {
                        insertPeriod(connection, 41L, 1L, 1L,
                                "2026-02-01 00:00:00+00", null);
                        connection.commit();
                        return new ConcurrentInsertOutcome(true, null, null);
                    } catch (SQLException failure) {
                        connection.rollback();
                        PSQLException postgresFailure = postgresException(failure);
                        return new ConcurrentInsertOutcome(
                                false,
                                postgresFailure.getSQLState(),
                                postgresFailure.getServerErrorMessage().getConstraint()
                        );
                    }
                }
            });

            assertThat(contenderReady.await(10, TimeUnit.SECONDS))
                    .as("the competing transaction must open its independent connection")
                    .isTrue();
            awaitUniqueIndexLock(contenderPid.get(), schema);
            firstTransaction.commit();

            ConcurrentInsertOutcome outcome = contender.get(15, TimeUnit.SECONDS);
            assertThat(outcome.committed()).isFalse();
            assertThat(outcome.sqlState()).isEqualTo("23505");
            assertThat(outcome.constraintName()).isEqualTo(OPEN_PERIOD_INDEX);
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS))
                    .as("the competing database operation must terminate within its bound")
                    .isTrue();
        }

        try (Connection verification = openConnection(schema)) {
            assertThat(openPeriodCount(verification, 1L)).isEqualTo(1L);
            assertThat(periodCount(verification, 1L)).isEqualTo(1L);
        }
    }

    @Test
    void shouldFailV6OnLegacyDuplicateOpenPeriodsWithoutChangingThem() throws Exception {
        String schema = newSchemaName();
        Flyway versionFive = migrate(schema, "5");
        assertThat(appliedVersions(versionFive))
                .containsExactly("1", "2", "3", "4", "5");

        try (Connection connection = openConnection(schema)) {
            insertFarm(connection, 1L);
            insertBovine(connection, 1L);
            insertPeriod(connection, 50L, 1L, 1L,
                    "2026-01-01 00:00:00+00", null);
            insertPeriod(connection, 51L, 1L, 1L,
                    "2026-02-01 00:00:00+00", null);
            assertThat(openPeriodCount(connection, 1L)).isEqualTo(2L);
        }

        Flyway allVersions = flyway(schema, null);
        Throwable failure = catchThrowable(() -> allVersions.migrate());
        assertThat(failure).isInstanceOf(FlywayException.class);
        assertThat(causeMessages(failure)).contains(OPEN_PERIOD_INDEX);

        try (Connection connection = openConnection(schema)) {
            assertThat(periodCount(connection, 1L)).isEqualTo(2L);
            assertThat(openPeriodCount(connection, 1L)).isEqualTo(2L);
            assertThat(periodIds(connection, 1L)).containsExactly(50L, 51L);
            assertThat(appliedVersions(allVersions))
                    .containsExactly("1", "2", "3", "4", "5");
            assertThat(indexExists(connection, schema, OPEN_PERIOD_INDEX)).isFalse();
        }
    }

    private static Flyway migrate(String schema, String target) {
        Flyway flyway = flyway(schema, target);
        flyway.migrate();
        return flyway;
    }

    private static Flyway flyway(String schema, String target) {
        var configuration = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .schemas(schema)
                .defaultSchema(schema);
        if (target != null) {
            configuration.target(target);
        }
        return configuration.load();
    }

    private static String newSchemaName() {
        return "ownership_period_v6_" + SCHEMA_SEQUENCE.incrementAndGet();
    }

    private static java.util.List<String> appliedVersions(Flyway flyway) {
        return Arrays.stream(flyway.info().applied())
                .map(info -> info.getVersion())
                .filter(Objects::nonNull)
                .map(version -> version.getVersion())
                .toList();
    }

    private static Connection openConnection(String schema) throws SQLException {
        Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO \"" + schema + "\"");
        }
        return connection;
    }

    private static void setStatementTimeout(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET statement_timeout TO '12s'");
        }
    }

    private static long nextSequenceId(Connection connection) throws SQLException {
        try (var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT nextval('bovine_ownership_period_id_seq')")) {
            result.next();
            return result.getLong(1);
        }
    }

    private static void insertFarm(Connection connection, long id) throws SQLException {
        try (var statement = connection.prepareStatement("""
                INSERT INTO farm (id, name, code_series, status, created_at, updated_at)
                VALUES (?, 'Migration test farm', ?, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """)) {
            statement.setLong(1, id);
            statement.setString(2, "MTF" + id);
            statement.executeUpdate();
        }
    }

    private static void insertBovine(Connection connection, long id) throws SQLException {
        try (var statement = connection.prepareStatement("""
                INSERT INTO bovine (
                    id, internal_code, name, sex, status, registration_category,
                    created_at, updated_at, origin_creator_name, origin_farm_name
                ) VALUES (?, ?, 'Migration test bovine', 'FEMALE', 'ACTIVE',
                          'UNREGISTERED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
                          'Migration test creator', 'Migration test origin farm')
                """)) {
            statement.setLong(1, id);
            statement.setString(2, "MIG-26-%04d".formatted(id));
            statement.executeUpdate();
        }
    }

    private static void insertPeriod(Connection connection, long id, long bovineId, long farmId,
                                     String startedAt, String endedAt) throws SQLException {
        String endedAtSql = endedAt == null ? "NULL" : "TIMESTAMP WITH TIME ZONE '" + endedAt + "'";
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO bovine_ownership_period (id, bovine_id, farm_id, started_at, ended_at)
                    VALUES (%d, %d, %d, TIMESTAMP WITH TIME ZONE '%s', %s)
                    """.formatted(id, bovineId, farmId, startedAt, endedAtSql));
        }
    }

    private static long periodCount(Connection connection, long bovineId) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM bovine_ownership_period WHERE bovine_id = ?")) {
            statement.setLong(1, bovineId);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    private static long openPeriodCount(Connection connection, long bovineId) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM bovine_ownership_period WHERE bovine_id = ? AND ended_at IS NULL")) {
            statement.setLong(1, bovineId);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    private static Instant periodEnd(Connection connection, long id) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT ended_at FROM bovine_ownership_period WHERE id = ?")) {
            statement.setLong(1, id);
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                var endedAt = result.getObject(1, java.time.OffsetDateTime.class);
                return endedAt == null ? null : endedAt.toInstant();
            }
        }
    }

    private static java.util.List<Long> periodIds(Connection connection, long bovineId) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT id FROM bovine_ownership_period WHERE bovine_id = ? ORDER BY id")) {
            statement.setLong(1, bovineId);
            try (var result = statement.executeQuery()) {
                java.util.List<Long> ids = new java.util.ArrayList<>();
                while (result.next()) {
                    ids.add(result.getLong(1));
                }
                return ids;
            }
        }
    }

    private static boolean indexExists(Connection connection, String schema, String indexName)
            throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM pg_indexes WHERE schemaname = ? AND indexname = ?")) {
            statement.setString(1, schema);
            statement.setString(2, indexName);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getLong(1) == 1L;
            }
        }
    }

    private static int backendPid(Connection connection) throws SQLException {
        try (var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT pg_backend_pid()")) {
            result.next();
            return result.getInt(1);
        }
    }

    private static void awaitUniqueIndexLock(int backendPid, String schema) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        String lastWaitEvent = null;
        try (Connection observer = openConnection(schema)) {
            while (System.nanoTime() < deadline) {
                try (var statement = observer.prepareStatement(
                        "SELECT wait_event_type FROM pg_stat_activity WHERE pid = ? AND state = 'active'")) {
                    statement.setInt(1, backendPid);
                    try (var result = statement.executeQuery()) {
                        if (result.next()) {
                            lastWaitEvent = result.getString(1);
                            if ("Lock".equals(lastWaitEvent)) {
                                return;
                            }
                        }
                    }
                }
                Thread.sleep(25);
            }
        }
        assertThat(lastWaitEvent)
                .as("the competing INSERT should be observed waiting on PostgreSQL's unique-index lock")
                .isEqualTo("Lock");
    }

    private static void assertPostgresViolation(SqlAction operation, String sqlState, String constraintName) {
        Throwable failure = catchThrowable(() -> operation.run());
        assertThat(failure).isNotNull();
        PSQLException postgresFailure = postgresException(failure);
        assertThat(postgresFailure.getSQLState()).isEqualTo(sqlState);
        assertThat(postgresFailure.getServerErrorMessage().getConstraint()).isEqualTo(constraintName);
    }

    private static PSQLException postgresException(Throwable failure) {
        Throwable current = failure;
        while (current != null && !(current instanceof PSQLException)) {
            current = current.getCause();
        }
        assertThat(current).isInstanceOf(PSQLException.class);
        return (PSQLException) current;
    }

    private static String causeMessages(Throwable failure) {
        StringBuilder messages = new StringBuilder();
        Throwable current = failure;
        while (current != null) {
            if (current.getMessage() != null) {
                messages.append(current.getMessage()).append('\n');
            }
            current = current.getCause();
        }
        return messages.toString();
    }

    @FunctionalInterface
    private interface SqlAction {
        void run() throws SQLException;
    }

    private record ConcurrentInsertOutcome(boolean committed, String sqlState, String constraintName) {
    }
}
