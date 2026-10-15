package com.bovigestor.application.service;

import com.bovigestor.adapter.out.persistence.entity.FarmJpaEntity;
import com.bovigestor.application.port.in.CreateFarmCommand;
import com.bovigestor.application.port.in.CreateFarmUseCase;
import com.bovigestor.domain.model.FarmId;
import com.bovigestor.domain.model.FarmStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class CreateFarmServiceIntegrationTest {

    @Autowired
    private CreateFarmUseCase createFarmUseCase;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldCreateAndPersistFarm() {
        // Arrange
        CreateFarmCommand command = new CreateFarmCommand(
                "Fazenda Santa Helena",
                "FSH"
        );

        // Act
        FarmId createdId = createFarmUseCase.create(command);

        // Assert
        assertNotNull(createdId);
        assertTrue(createdId.value() > 0);

        FarmJpaEntity savedFarm = entityManager.find(
                FarmJpaEntity.class,
                createdId.value()
        );

        assertNotNull(savedFarm);

        assertAll(
                () -> assertEquals(createdId.value(), savedFarm.getId()),
                () -> assertEquals("Fazenda Santa Helena", savedFarm.getName()),
                () -> assertEquals("FSH", savedFarm.getCodeSeries()),
                () -> assertEquals(FarmStatus.ACTIVE, savedFarm.getStatus()),
                () -> assertNotNull(savedFarm.getCreatedAt()),
                () -> assertEquals(savedFarm.getCreatedAt(), savedFarm.getUpdatedAt())
        );
    }
}
