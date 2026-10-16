package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.entity.FarmJpaEntity;
import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;
import com.bovigestor.domain.model.FarmStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class JpaSaveFarmAdapterTest {

    @Autowired
    private JpaSaveFarmAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void shouldPersistFarm() {
        // Arrange
        FarmId id = new FarmId(1001L);

        Instant createdAt = Instant.parse("2026-10-14T12:00:00Z");

        Farm farm = Farm.create(
                id,
                "Fazenda Boa Vista",
                "BVL",
                createdAt
        );
        // Act
        adapter.save(farm);

        entityManager.flush();
        entityManager.clear();

        // Assert
        FarmJpaEntity savedFarm = entityManager.find(FarmJpaEntity.class, id.value());

        assertNotNull(savedFarm);
        assertAll(
                () -> assertEquals(id.value(), savedFarm.getId()),
                () -> assertEquals("Fazenda Boa Vista", savedFarm.getName()),
                () -> assertEquals("BVL", savedFarm.getCodeSeries()),
                () -> assertEquals(FarmStatus.ACTIVE, savedFarm.getStatus()),
                () -> assertEquals(createdAt, savedFarm.getCreatedAt()),
                () -> assertEquals(createdAt, savedFarm.getUpdatedAt())
        );
    }
}
