package com.bovigestor.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

final class BovineConstraintTestScenarios {

    private static final AtomicLong TEST_IDS = new AtomicLong(4_000_000L);

    private BovineConstraintTestScenarios() {
    }

    static Stream<String> checkConstraintScenarios() {
        return Stream.of(
                "non-positive-id",
                "blank-name",
                "invalid-sex",
                "invalid-status",
                "invalid-registration-category",
                "birth-after-created-at",
                "registered-details-required",
                "unregistered-with-details",
                "po-parentage-required",
                "pc-parentage-required",
                "father-self-reference",
                "mother-self-reference",
                "blank-origin-creator-name",
                "blank-origin-farm-name",
                "non-positive-origin-farm-id"
        );
    }

    static Stream<String> requiredColumnScenarios() {
        return Stream.of(
                "internal_code",
                "name",
                "sex",
                "status",
                "registration_category",
                "created_at",
                "updated_at",
                "origin_creator_name",
                "origin_farm_name"
        );
    }

    static Map<String, Object> validInsertValues() {
        long id = TEST_IDS.getAndIncrement();
        Map<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("internal_code", "TST-26-%08d".formatted(id));
        values.put("name", "Bovino de teste");
        values.put("sex", "FEMALE");
        values.put("breed", null);
        values.put("coat_color", null);
        values.put("birth_date", null);
        values.put("status", "ACTIVE");
        values.put("ear_tag", null);
        values.put("registration_number", null);
        values.put("registration_association", null);
        values.put("registration_category", "UNREGISTERED");
        values.put("father_id", null);
        values.put("mother_id", null);
        values.put("father_registration_number", null);
        values.put("mother_registration_number", null);
        values.put("created_at", Instant.parse("2026-10-19T12:00:00Z"));
        values.put("updated_at", Instant.parse("2026-10-19T12:00:00Z"));
        values.put("origin_creator_name", "Criador de teste");
        values.put("origin_farm_name", "Fazenda de teste");
        values.put("origin_farm_id", null);
        return values;
    }

    static void applyCheckViolation(Map<String, Object> values, String scenario) {
        switch (scenario) {
            case "non-positive-id" -> values.put("id", -(Long) values.get("id"));
            case "blank-name" -> values.put("name", "   ");
            case "invalid-sex" -> values.put("sex", "UNKNOWN");
            case "invalid-status" -> values.put("status", "UNKNOWN");
            case "invalid-registration-category" -> {
                values.put("registration_category", "UNKNOWN");
                values.put("registration_number", "REG-TEST");
                values.put("registration_association", "ABC");
            }
            case "birth-after-created-at" -> values.put("birth_date", LocalDate.of(2026, 10, 20));
            case "registered-details-required" -> values.put("registration_category", "PA");
            case "unregistered-with-details" -> {
                values.put("registration_number", "REG-TEST");
                values.put("registration_association", "ABC");
            }
            case "po-parentage-required", "pc-parentage-required" -> {
                values.put("registration_category", scenario.startsWith("po-") ? "PO" : "PC");
                values.put("registration_number", "REG-TEST");
                values.put("registration_association", "ABC");
            }
            case "father-self-reference" -> values.put("father_id", values.get("id"));
            case "mother-self-reference" -> values.put("mother_id", values.get("id"));
            case "blank-origin-creator-name" -> values.put("origin_creator_name", "   ");
            case "blank-origin-farm-name" -> values.put("origin_farm_name", "   ");
            case "non-positive-origin-farm-id" -> values.put("origin_farm_id", 0L);
            default -> throw new IllegalArgumentException("Unknown CHECK scenario: " + scenario);
        }
    }

    static void insert(EntityManager entityManager, Map<String, Object> values) {
        String columns = String.join(", ", values.keySet());
        String parameters = String.join(", ", values.keySet().stream()
                .map(name -> ":" + name)
                .toList());
        var query = entityManager.createNativeQuery(
                "INSERT INTO bovine (" + columns + ") VALUES (" + parameters + ")"
        );
        values.forEach(query::setParameter);
        query.executeUpdate();
    }

    static void assertConstraintViolation(Runnable operation, String... expectedSqlStates) {
        Throwable failure = catchThrowable(operation::run);

        assertThat(failure).isInstanceOf(PersistenceException.class);
        SQLException sqlException = findSqlException(failure);
        assertThat((Object) sqlException)
                .as("the persistence error should retain the database SQL state")
                .isNotNull();
        assertThat(Arrays.asList(expectedSqlStates)).contains(sqlException.getSQLState());
    }

    private static SQLException findSqlException(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof SQLException sqlException) {
                return sqlException;
            }
            current = current.getCause();
        }
        return null;
    }
}
