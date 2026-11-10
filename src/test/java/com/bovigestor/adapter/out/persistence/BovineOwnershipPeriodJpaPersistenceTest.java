package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.entity.BovineOwnershipPeriodJpaEntity;
import com.bovigestor.adapter.out.persistence.mapper.BovineOwnershipPeriodJpaMapper;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineOwnershipPeriod;
import com.bovigestor.domain.model.BovineOwnershipPeriodId;
import com.bovigestor.domain.model.FarmId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
class BovineOwnershipPeriodJpaPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void shouldPersistAndReconstituteClosedPeriodThroughHibernate() {
        // Arrange
        long periodId = 9_500_001L;
        long bovineId = 8_500_001L;
        long farmId = 7_500_001L;
        insertFarmAndBovine(farmId, bovineId, "P001", "OWN-TEST-001");

        Instant startedAt = Instant.parse("2026-06-01T12:00:00Z");
        Instant endedAt = Instant.parse("2026-06-15T12:00:00Z");
        BovineOwnershipPeriod period = BovineOwnershipPeriod.reconstitute(
                new BovineOwnershipPeriodId(periodId),
                new BovineId(bovineId),
                new FarmId(farmId),
                startedAt,
                endedAt
        );

        // Act
        entityManager.persist(BovineOwnershipPeriodJpaMapper.toEntity(period));
        entityManager.flush();
        entityManager.clear();

        BovineOwnershipPeriodJpaEntity persistedEntity = entityManager.find(
                BovineOwnershipPeriodJpaEntity.class,
                periodId
        );
        assertNotNull(persistedEntity);
        BovineOwnershipPeriod restoredPeriod = BovineOwnershipPeriodJpaMapper
                .toDomain(persistedEntity);

        // Assert
        assertAll(
                () -> assertEquals(new BovineOwnershipPeriodId(periodId), restoredPeriod.id()),
                () -> assertEquals(new BovineId(bovineId), restoredPeriod.bovineId()),
                () -> assertEquals(new FarmId(farmId), restoredPeriod.farmId()),
                () -> assertEquals(startedAt, restoredPeriod.startedAt()),
                () -> assertEquals(endedAt, restoredPeriod.endedAt())
        );
    }

    @Test
    @Transactional
    void shouldPersistAndReconstituteOpenPeriodWithNullEndThroughHibernate() {
        // Arrange
        long periodId = 9_500_002L;
        long bovineId = 8_500_002L;
        long farmId = 7_500_002L;
        insertFarmAndBovine(farmId, bovineId, "P002", "OWN-TEST-002");

        Instant startedAt = Instant.parse("2026-07-01T09:30:00Z");
        BovineOwnershipPeriod period = BovineOwnershipPeriod.open(
                new BovineOwnershipPeriodId(periodId),
                new BovineId(bovineId),
                new FarmId(farmId),
                startedAt
        );

        // Act
        entityManager.persist(BovineOwnershipPeriodJpaMapper.toEntity(period));
        entityManager.flush();
        entityManager.clear();

        BovineOwnershipPeriodJpaEntity persistedEntity = entityManager.find(
                BovineOwnershipPeriodJpaEntity.class,
                periodId
        );
        assertNotNull(persistedEntity);
        BovineOwnershipPeriod restoredPeriod = BovineOwnershipPeriodJpaMapper
                .toDomain(persistedEntity);

        // Assert
        assertAll(
                () -> assertEquals(new BovineOwnershipPeriodId(periodId), restoredPeriod.id()),
                () -> assertEquals(new BovineId(bovineId), restoredPeriod.bovineId()),
                () -> assertEquals(new FarmId(farmId), restoredPeriod.farmId()),
                () -> assertEquals(startedAt, restoredPeriod.startedAt()),
                () -> assertNull(restoredPeriod.endedAt())
        );
    }

    private void insertFarmAndBovine(
            long farmId,
            long bovineId,
            String codeSeries,
            String internalCode
    ) {
        jdbcTemplate.update("""
                INSERT INTO farm (id, name, code_series, status, created_at, updated_at)
                VALUES (?, 'Ownership period test farm', ?, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, farmId, codeSeries);

        jdbcTemplate.update("""
                INSERT INTO bovine (
                    id, internal_code, name, sex, status, registration_category,
                    created_at, updated_at, origin_creator_name, origin_farm_name
                ) VALUES (?, ?, 'Ownership period test bovine', 'FEMALE', 'ACTIVE',
                          'UNREGISTERED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
                          'Test creator', 'Test origin farm')
                """, bovineId, internalCode);
    }
}
