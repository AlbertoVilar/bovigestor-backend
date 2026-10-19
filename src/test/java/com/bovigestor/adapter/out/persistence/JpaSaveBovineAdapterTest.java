package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.entity.BovineJpaEntity;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineInternalCode;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineRegistrationData;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
class JpaSaveBovineAdapterTest {

    @Autowired
    private JpaSaveBovineAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void shouldPersistBovine() {
        BovineId id = new BovineId(3_100_001L);
        Instant createdAt = Instant.parse("2026-10-14T12:00:00Z");
        Bovine bovine = Bovine.createManually(
                id,
                new BovineInternalCode("BVL-26-3001"),
                new BovineRegistrationData(
                        "Faisca",
                        BovineSex.FEMALE,
                        "Nelore",
                        "Branca",
                        LocalDate.of(2024, 1, 15),
                        BovineStatus.ACTIVE,
                        "TAG-3001",
                        "REG-3001",
                        "ABC",
                        BovineRegistrationCategory.PO,
                        null,
                        null,
                        "EXT-F-3001",
                        "EXT-M-3001"
                ),
                createdAt
        );

        adapter.save(bovine);
        entityManager.flush();
        entityManager.clear();

        BovineJpaEntity savedBovine = entityManager.find(BovineJpaEntity.class, id.value());

        assertNotNull(savedBovine);
        assertAll(
                () -> assertEquals(id.value(), savedBovine.getId()),
                () -> assertEquals("BVL-26-3001", savedBovine.getInternalCode()),
                () -> assertEquals("Faisca", savedBovine.getName()),
                () -> assertEquals(BovineSex.FEMALE, savedBovine.getSex()),
                () -> assertEquals("Nelore", savedBovine.getBreed()),
                () -> assertEquals("Branca", savedBovine.getCoatColor()),
                () -> assertEquals(LocalDate.of(2024, 1, 15), savedBovine.getBirthDate()),
                () -> assertEquals(BovineStatus.ACTIVE, savedBovine.getStatus()),
                () -> assertEquals("TAG-3001", savedBovine.getEarTag()),
                () -> assertEquals("REG-3001", savedBovine.getRegistrationNumber()),
                () -> assertEquals("ABC", savedBovine.getRegistrationAssociation()),
                () -> assertEquals(
                        BovineRegistrationCategory.PO,
                        savedBovine.getRegistrationCategory()
                ),
                () -> assertNull(savedBovine.getFatherId()),
                () -> assertNull(savedBovine.getMotherId()),
                () -> assertEquals("EXT-F-3001", savedBovine.getFatherRegistrationNumber()),
                () -> assertEquals("EXT-M-3001", savedBovine.getMotherRegistrationNumber()),
                () -> assertEquals(createdAt, savedBovine.getCreatedAt()),
                () -> assertEquals(createdAt, savedBovine.getUpdatedAt())
        );
    }
}
