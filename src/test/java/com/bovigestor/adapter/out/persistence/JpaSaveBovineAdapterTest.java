package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.entity.BovineJpaEntity;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineInternalCode;
import com.bovigestor.domain.model.BovineOrigin;
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
                createdAt,
                testOrigin()
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
                () -> assertEquals(createdAt, savedBovine.getUpdatedAt()),
                () -> assertEquals("Criador de teste", savedBovine.getOrigin().getCreatorName()),
                () -> assertEquals("Fazenda de teste", savedBovine.getOrigin().getOriginFarmName()),
                () -> assertNull(savedBovine.getOrigin().getOriginFarmId())
        );
    }

    @Test
    @Transactional
    void shouldPersistOriginNamesAtMaximumColumnLength() {
        String creatorName = "C".repeat(255);
        String originFarmName = "F".repeat(255);
        Bovine bovine = Bovine.createManually(
                new BovineId(3_100_002L),
                new BovineInternalCode("BVL-26-3002"),
                new BovineRegistrationData(
                        "Novilha", BovineSex.FEMALE, null, null, null, BovineStatus.ACTIVE,
                        null, null, null, BovineRegistrationCategory.UNREGISTERED,
                        null, null, null, null
                ),
                Instant.parse("2026-10-14T12:00:00Z"),
                new BovineOrigin(creatorName, originFarmName, null)
        );

        adapter.save(bovine);
        entityManager.flush();
        entityManager.clear();

        BovineJpaEntity savedBovine = entityManager.find(BovineJpaEntity.class, 3_100_002L);
        assertNotNull(savedBovine);
        assertEquals(creatorName, savedBovine.getOrigin().getCreatorName());
        assertEquals(originFarmName, savedBovine.getOrigin().getOriginFarmName());
    }

    private static BovineOrigin testOrigin() {
        return new BovineOrigin("Criador de teste", "Fazenda de teste", null);
    }
}
