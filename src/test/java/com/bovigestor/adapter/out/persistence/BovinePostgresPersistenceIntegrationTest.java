package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.entity.BovineJpaEntity;
import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.application.port.in.CreateBovineUseCase;
import com.bovigestor.application.port.out.BovineIdGenerationPort;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import jakarta.persistence.EntityManager;
import org.flywaydb.core.Flyway;
import org.postgresql.util.PSQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.bovigestor.adapter.out.persistence.BovineConstraintTestScenarios.applyCheckViolation;
import static com.bovigestor.adapter.out.persistence.BovineConstraintTestScenarios.insert;
import static com.bovigestor.adapter.out.persistence.BovineConstraintTestScenarios.validInsertValues;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class BovinePostgresPersistenceIntegrationTest {

    private static final AtomicLong INTERNAL_CODE_SEQUENCE = new AtomicLong(5_000L);

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17")
    ).withDatabaseName("bovigestor_validation")
            .withUsername("bovine_test")
            .withPassword("bovine_test_password");

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private BovineIdGenerationPort bovineIdGenerationPort;

    @Autowired
    private CreateBovineUseCase createBovineUseCase;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Test
    void shouldApplyV1AndV2ToCleanPostgresAndValidateHibernateMapping() {
        String serverVersion = jdbcTemplate.queryForObject("SELECT version()", String.class);
        String mappedPort = Integer.toString(POSTGRES.getMappedPort(5432));
        System.out.printf(
                "PostgreSQL Testcontainers: container=%s, image=%s, hostPort=%s, server=%s%n",
                POSTGRES.getContainerId(),
                POSTGRES.getDockerImageName(),
                mappedPort,
                serverVersion
        );

        assertThat(serverVersion).startsWith("PostgreSQL 17");
        assertThat(Arrays.stream(flyway.info().applied())
                .map(info -> info.getVersion().getVersion())
                .toList())
                .containsExactly("1", "2");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT to_regclass('public.bovine') IS NOT NULL",
                Boolean.class
        )).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT to_regclass('public.bovine_id_seq') IS NOT NULL",
                Boolean.class
        )).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_schema = 'public' AND table_name = 'bovine'",
                Integer.class
        )).isEqualTo(18);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT data_type FROM information_schema.columns "
                        + "WHERE table_schema = 'public' AND table_name = 'bovine' "
                        + "AND column_name = 'created_at'",
                String.class
        )).isEqualTo("timestamp with time zone");
    }

    @Test
    void shouldGenerateDistinctPositiveIdsFromPostgresSequence() {
        BovineId first = bovineIdGenerationPort.nextId();
        BovineId second = bovineIdGenerationPort.nextId();

        assertThat(first.value()).isPositive();
        assertThat(second.value()).isPositive();
        assertThat(second).isNotEqualTo(first);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT last_value FROM bovine_id_seq",
                Long.class
        )).isGreaterThanOrEqualTo(second.value());
    }

    @Test
    @Transactional
    void shouldPersistAndReadAllEighteenBovineAttributesInPostgres() {
        CreateBovineCommand command = new CreateBovineCommand(
                "PostgreSQL Round Trip",
                nextInternalCode(),
                BovineSex.FEMALE,
                "Nelore",
                "Branca",
                LocalDate.of(2024, 1, 15),
                BovineStatus.ACTIVE,
                "PG-TAG-1",
                "PG-REG-1",
                "ABC",
                BovineRegistrationCategory.PO,
                null,
                null,
                "PG-EXT-F-1",
                "PG-EXT-M-1"
        );

        BovineId createdId = createBovineUseCase.create(command);
        entityManager.flush();
        entityManager.clear();

        BovineJpaEntity saved = entityManager.find(BovineJpaEntity.class, createdId.value());

        assertNotNull(saved);
        assertEquals(createdId.value(), saved.getId());
        assertEquals(command.internalCode(), saved.getInternalCode());
        assertEquals(command.name(), saved.getName());
        assertEquals(command.sex(), saved.getSex());
        assertEquals(command.breed(), saved.getBreed());
        assertEquals(command.coatColor(), saved.getCoatColor());
        assertEquals(command.birthDate(), saved.getBirthDate());
        assertEquals(command.status(), saved.getStatus());
        assertEquals(command.earTag(), saved.getEarTag());
        assertEquals(command.registrationNumber(), saved.getRegistrationNumber());
        assertEquals(command.registrationAssociation(), saved.getRegistrationAssociation());
        assertEquals(command.registrationCategory(), saved.getRegistrationCategory());
        assertNull(saved.getFatherId());
        assertNull(saved.getMotherId());
        assertEquals(command.fatherRegistrationNumber(), saved.getFatherRegistrationNumber());
        assertEquals(command.motherRegistrationNumber(), saved.getMotherRegistrationNumber());
        assertNotNull(saved.getCreatedAt());
        assertEquals(saved.getCreatedAt(), saved.getUpdatedAt());
    }

    @Test
    @Transactional
    void shouldPersistOptionalAttributesAsNull() {
        BovineId createdId = createBovineUseCase.create(unregisteredCommand("Optional Nulls"));
        entityManager.flush();
        entityManager.clear();

        BovineJpaEntity saved = entityManager.find(BovineJpaEntity.class, createdId.value());

        assertNotNull(saved);
        assertNull(saved.getBreed());
        assertNull(saved.getCoatColor());
        assertNull(saved.getBirthDate());
        assertNull(saved.getEarTag());
        assertNull(saved.getRegistrationNumber());
        assertNull(saved.getRegistrationAssociation());
        assertNull(saved.getFatherId());
        assertNull(saved.getMotherId());
        assertNull(saved.getFatherRegistrationNumber());
        assertNull(saved.getMotherRegistrationNumber());
    }

    @Test
    @Transactional
    void shouldPersistValidProvisionalRegistrationWithoutParentage() {
        CreateBovineCommand provisional = new CreateBovineCommand(
                "PG Provisional Registration",
                nextInternalCode(),
                BovineSex.FEMALE,
                null,
                null,
                null,
                BovineStatus.ACTIVE,
                null,
                "PG-PA-REG",
                "ABC",
                BovineRegistrationCategory.PA,
                null,
                null,
                null,
                null
        );

        BovineId createdId = createBovineUseCase.create(provisional);
        entityManager.flush();
        entityManager.clear();

        BovineJpaEntity saved = entityManager.find(BovineJpaEntity.class, createdId.value());

        assertNotNull(saved);
        assertEquals(BovineRegistrationCategory.PA, saved.getRegistrationCategory());
        assertNull(saved.getFatherId());
        assertNull(saved.getMotherId());
        assertNull(saved.getFatherRegistrationNumber());
        assertNull(saved.getMotherRegistrationNumber());
    }

    @Test
    @Transactional
    void shouldPersistValidLocalFatherAndMotherForeignKeys() {
        BovineId fatherId = createBovineUseCase.create(unregisteredCommand("PG Father"));
        BovineId motherId = createBovineUseCase.create(unregisteredCommand("PG Mother"));
        CreateBovineCommand child = new CreateBovineCommand(
                "PG Child",
                nextInternalCode(),
                BovineSex.FEMALE,
                null,
                null,
                LocalDate.of(2025, 2, 10),
                BovineStatus.ACTIVE,
                null,
                "PG-CHILD-REG",
                "ABC",
                BovineRegistrationCategory.PC,
                fatherId,
                motherId,
                null,
                null
        );

        BovineId childId = createBovineUseCase.create(child);
        entityManager.flush();
        entityManager.clear();

        BovineJpaEntity saved = entityManager.find(BovineJpaEntity.class, childId.value());
        assertNotNull(saved);
        assertEquals(fatherId.value(), saved.getFatherId());
        assertEquals(motherId.value(), saved.getMotherId());
    }

    @Test
    void shouldRollbackBovineAfterFlushInsideUseCaseTransactionBoundary() {
        CreateBovineCommand command = unregisteredCommand("PG Rollback");
        AtomicReference<BovineId> createdId = new AtomicReference<>();

        boolean rolledBack = false;
        try {
            transactionTemplate.executeWithoutResult(status -> {
                BovineId id = createBovineUseCase.create(command);
                createdId.set(id);
                entityManager.flush();
                entityManager.clear();
                assertThat(entityManager.find(BovineJpaEntity.class, id.value())).isNotNull();
                throw new IllegalStateException("Intentional PostgreSQL rollback verification");
            });
        } catch (IllegalStateException expected) {
            assertThat(expected).hasMessage("Intentional PostgreSQL rollback verification");
            rolledBack = true;
        }

        assertThat(rolledBack).isTrue();
        assertNotNull(createdId.get());
        entityManager.clear();
        assertNull(entityManager.find(BovineJpaEntity.class, createdId.get().value()));
    }

    @Test
    @Transactional
    void shouldRejectDuplicateGlobalInternalCodeWithPostgresUniqueSqlState() {
        Map<String, Object> first = validInsertValues();
        Map<String, Object> duplicate = validInsertValues();
        duplicate.put("internal_code", first.get("internal_code"));
        insert(entityManager, first);

        assertPostgresConstraintViolation(
                () -> insert(entityManager, duplicate), "23505", "uq_bovine_internal_code"
        );
    }

    @Test
    @Transactional
    void shouldRejectDuplicatePrimaryKeyWithPostgresUniqueSqlState() {
        Map<String, Object> first = validInsertValues();
        Map<String, Object> duplicate = validInsertValues();
        duplicate.put("id", first.get("id"));
        insert(entityManager, first);

        assertPostgresConstraintViolation(() -> insert(entityManager, duplicate), "23505", "pk_bovine");
    }

    @ParameterizedTest
    @MethodSource("parentForeignKeyColumns")
    @Transactional
    void shouldRejectUnknownParentIdsWithPostgresForeignKeySqlState(String parentColumn) {
        Map<String, Object> values = validInsertValues();
        values.put(parentColumn, 9_999_999_999L);

        assertPostgresConstraintViolation(
                () -> insert(entityManager, values),
                "23503",
                parentColumn.equals("father_id") ? "fk_bovine_father" : "fk_bovine_mother"
        );
    }

    @ParameterizedTest
    @MethodSource("com.bovigestor.adapter.out.persistence.BovineConstraintTestScenarios#checkConstraintScenarios")
    @Transactional
    void shouldRejectEachBovineCheckViolationWithPostgresSqlState(String scenario) {
        Map<String, Object> values = validInsertValues();
        applyCheckViolation(values, scenario);

        assertPostgresConstraintViolation(() -> insert(entityManager, values), "23514", checkConstraintName(scenario));
    }

    @ParameterizedTest
    @MethodSource("com.bovigestor.adapter.out.persistence.BovineConstraintTestScenarios#requiredColumnScenarios")
    @Transactional
    void shouldRejectNullForEachRequiredColumnWithPostgresSqlState(String column) {
        Map<String, Object> values = validInsertValues();
        values.put(column, null);

        assertPostgresColumnViolation(() -> insert(entityManager, values), column);
    }

    private static void assertPostgresConstraintViolation(
            Runnable operation,
            String expectedSqlState,
            String expectedConstraint
    ) {
        PSQLException failure = postgresException(operation);
        assertThat(failure.getSQLState()).isEqualTo(expectedSqlState);
        assertThat(failure.getServerErrorMessage().getConstraint()).isEqualTo(expectedConstraint);
    }

    private static void assertPostgresColumnViolation(Runnable operation, String expectedColumn) {
        PSQLException failure = postgresException(operation);
        assertThat(failure.getSQLState()).isEqualTo("23502");
        assertThat(failure.getServerErrorMessage().getColumn()).isEqualTo(expectedColumn);
    }

    private static PSQLException postgresException(Runnable operation) {
        Throwable failure = org.assertj.core.api.Assertions.catchThrowable(operation::run);
        assertThat(failure).isNotNull();

        Throwable current = failure;
        while (current != null && !(current instanceof PSQLException)) {
            current = current.getCause();
        }

        assertThat(current)
                .as("the persistence failure should retain PostgreSQL server diagnostics")
                .isInstanceOf(PSQLException.class);
        return (PSQLException) current;
    }

    private static String checkConstraintName(String scenario) {
        return switch (scenario) {
            case "non-positive-id" -> "ck_bovine_id_positive";
            case "blank-name" -> "ck_bovine_name_not_blank";
            case "invalid-sex" -> "ck_bovine_sex";
            case "invalid-status" -> "ck_bovine_status";
            case "invalid-registration-category" -> "ck_bovine_registration_category";
            case "birth-after-created-at" -> "ck_bovine_birth_date_not_after_created_at";
            case "registered-details-required" -> "ck_bovine_registered_details_required";
            case "unregistered-with-details" -> "ck_bovine_unregistered_without_details";
            case "po-parentage-required", "pc-parentage-required" -> "ck_bovine_po_pc_parentage_required";
            case "father-self-reference" -> "ck_bovine_father_not_self";
            case "mother-self-reference" -> "ck_bovine_mother_not_self";
            default -> throw new IllegalArgumentException("Unknown CHECK scenario: " + scenario);
        };
    }

    private static java.util.stream.Stream<String> parentForeignKeyColumns() {
        return java.util.stream.Stream.of("father_id", "mother_id");
    }

    private CreateBovineCommand unregisteredCommand(String name) {
        return new CreateBovineCommand(
                name,
                nextInternalCode(),
                BovineSex.MALE,
                null,
                null,
                null,
                BovineStatus.ACTIVE,
                null,
                null,
                null,
                BovineRegistrationCategory.UNREGISTERED,
                null,
                null,
                null,
                null
        );
    }

    private String nextInternalCode() {
        return "PGT-26-%08d".formatted(INTERNAL_CODE_SEQUENCE.getAndIncrement());
    }
}
