package com.bovigestor.application.service;

import com.bovigestor.adapter.out.persistence.entity.BovineJpaEntity;
import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.application.port.in.CreateBovineUseCase;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class CreateBovineServiceIntegrationTest {

    @Autowired
    private CreateBovineUseCase createBovineUseCase;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldCreateAndPersistBovineWithExternalParentReferences() {
        CreateBovineCommand command = new CreateBovineCommand(
                "Mimosa",
                createInternalCode(),
                BovineSex.FEMALE,
                "Nelore",
                "Branca",
                LocalDate.of(2024, 1, 15),
                BovineStatus.ACTIVE,
                "TAG-4001",
                "REG-4001",
                "ABC",
                BovineRegistrationCategory.PO,
                null,
                null,
                "EXT-F-4001",
                "EXT-M-4001"
        );

        BovineId createdId = createBovineUseCase.create(command);

        assertNotNull(createdId);
        assertTrue(createdId.value() > 0);

        entityManager.clear();
        BovineJpaEntity savedBovine = entityManager.find(
                BovineJpaEntity.class,
                createdId.value()
        );

        assertNotNull(savedBovine);
        assertAll(
                () -> assertEquals(createdId.value(), savedBovine.getId()),
                () -> assertEquals(command.internalCode(), savedBovine.getInternalCode()),
                () -> assertEquals(command.name(), savedBovine.getName()),
                () -> assertEquals(command.sex(), savedBovine.getSex()),
                () -> assertEquals(command.breed(), savedBovine.getBreed()),
                () -> assertEquals(command.coatColor(), savedBovine.getCoatColor()),
                () -> assertEquals(command.birthDate(), savedBovine.getBirthDate()),
                () -> assertEquals(command.status(), savedBovine.getStatus()),
                () -> assertEquals(command.earTag(), savedBovine.getEarTag()),
                () -> assertEquals(command.registrationNumber(), savedBovine.getRegistrationNumber()),
                () -> assertEquals(
                        command.registrationAssociation(),
                        savedBovine.getRegistrationAssociation()
                ),
                () -> assertEquals(
                        command.registrationCategory(),
                        savedBovine.getRegistrationCategory()
                ),
                () -> assertEquals(command.fatherId(), savedBovine.getFatherId() == null
                        ? null
                        : new BovineId(savedBovine.getFatherId())),
                () -> assertEquals(command.motherId(), savedBovine.getMotherId() == null
                        ? null
                        : new BovineId(savedBovine.getMotherId())),
                () -> assertEquals(
                        command.fatherRegistrationNumber(),
                        savedBovine.getFatherRegistrationNumber()
                ),
                () -> assertEquals(
                        command.motherRegistrationNumber(),
                        savedBovine.getMotherRegistrationNumber()
                ),
                () -> assertNotNull(savedBovine.getCreatedAt()),
                () -> assertEquals(savedBovine.getCreatedAt(), savedBovine.getUpdatedAt())
        );
    }

    @Test
    @Transactional
    void shouldCreateAndPersistBovineWithLocalParentIds() {
        BovineId fatherId = createBovineUseCase.create(unregisteredCommand("Pai"));
        BovineId motherId = createBovineUseCase.create(unregisteredCommand("Mãe"));
        CreateBovineCommand command = new CreateBovineCommand(
                "Bezerra",
                createInternalCode(),
                BovineSex.FEMALE,
                "Nelore",
                "Vermelha",
                LocalDate.of(2025, 2, 10),
                BovineStatus.ACTIVE,
                "TAG-LOCAL",
                "REG-LOCAL",
                "ABC",
                BovineRegistrationCategory.PC,
                fatherId,
                motherId,
                null,
                null
        );

        BovineId createdId = createBovineUseCase.create(command);

        entityManager.flush();
        entityManager.clear();
        BovineJpaEntity savedBovine = entityManager.find(
                BovineJpaEntity.class,
                createdId.value()
        );

        assertNotNull(savedBovine);
        assertEquals(fatherId.value(), savedBovine.getFatherId());
        assertEquals(motherId.value(), savedBovine.getMotherId());
    }

    private CreateBovineCommand unregisteredCommand(String name) {
        return new CreateBovineCommand(
                name,
                createInternalCode(),
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

    private String createInternalCode() {
        long sequence = ThreadLocalRandom.current().nextLong(1, 100_000_000L);
        return "TST-26-%08d".formatted(sequence);
    }
}
