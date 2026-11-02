package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.entity.BovineJpaEntity;
import com.bovigestor.adapter.out.persistence.entity.BovineOriginEmbeddable;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

import static com.bovigestor.adapter.out.persistence.BovineConstraintTestScenarios.applyCheckViolation;
import static com.bovigestor.adapter.out.persistence.BovineConstraintTestScenarios.assertConstraintViolation;
import static com.bovigestor.adapter.out.persistence.BovineConstraintTestScenarios.insert;
import static com.bovigestor.adapter.out.persistence.BovineConstraintTestScenarios.validInsertValues;

@SpringBootTest
@ActiveProfiles("test")
class BovinePersistenceConstraintsIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void shouldRejectDuplicateGlobalInternalCode() {
        Instant now = Instant.parse("2026-10-19T12:00:00Z");
        entityManager.persist(entity(3_200_001L, "TST-26-3201", null, now));
        entityManager.flush();

        entityManager.persist(entity(3_200_002L, "TST-26-3201", null, now));

        assertConstraintViolation(entityManager::flush, "23505");
    }

    @Test
    @Transactional
    void shouldRejectParentIdThatDoesNotReferenceAnotherBovine() {
        Instant now = Instant.parse("2026-10-19T12:00:00Z");
        entityManager.persist(entity(3_200_003L, "TST-26-3203", 9_999_999L, now));

        assertConstraintViolation(entityManager::flush, "23503", "23506");
    }

    @Test
    @Transactional
    void shouldRejectBovineAsItsOwnParent() {
        Instant now = Instant.parse("2026-10-19T12:00:00Z");
        entityManager.persist(entity(3_200_004L, "TST-26-3204", 3_200_004L, now));

        assertConstraintViolation(entityManager::flush, "23514", "23513");
    }

    @ParameterizedTest
    @MethodSource("com.bovigestor.adapter.out.persistence.BovineConstraintTestScenarios#checkConstraintScenarios")
    @Transactional
    void shouldRejectInvalidValuesForBovineCheckConstraints(String scenario) {
        Map<String, Object> values = validInsertValues();
        applyCheckViolation(values, scenario);

        assertConstraintViolation(() -> insert(entityManager, values), "23514", "23513");
    }

    @ParameterizedTest
    @MethodSource("com.bovigestor.adapter.out.persistence.BovineConstraintTestScenarios#requiredColumnScenarios")
    @Transactional
    void shouldRejectNullForRequiredBovineColumns(String column) {
        Map<String, Object> values = validInsertValues();
        values.put(column, null);

        assertConstraintViolation(() -> insert(entityManager, values), "23502");
    }

    private BovineJpaEntity entity(Long id, String internalCode, Long fatherId, Instant now) {
        return new BovineJpaEntity(
                id,
                internalCode,
                "Bovino de teste",
                BovineSex.FEMALE,
                null,
                null,
                null,
                BovineStatus.ACTIVE,
                null,
                null,
                null,
                BovineRegistrationCategory.UNREGISTERED,
                fatherId,
                null,
                null,
                null,
                now,
                now,
                new BovineOriginEmbeddable("Criador de teste", "Fazenda de teste", null)
        );
    }

}
