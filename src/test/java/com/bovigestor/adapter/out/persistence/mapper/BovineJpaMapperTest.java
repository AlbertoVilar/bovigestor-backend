package com.bovigestor.adapter.out.persistence.mapper;

import com.bovigestor.adapter.out.persistence.entity.BovineJpaEntity;
import com.bovigestor.adapter.out.persistence.entity.BovineOriginEmbeddable;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineInternalCode;
import com.bovigestor.domain.model.BovineOrigin;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineRegistrationData;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import com.bovigestor.domain.model.FarmId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class BovineJpaMapperTest {

    @Test
    void shouldMapAllBovineAttributesToJpaEntity() {
        Instant createdAt = Instant.parse("2026-06-01T12:00:00Z");
        Bovine bovine = Bovine.createManually(
                new BovineId(42L),
                new BovineInternalCode("BVL-26-0042"),
                new BovineRegistrationData(
                        "Mimosa",
                        BovineSex.FEMALE,
                        "Nelore",
                        "Branca",
                        LocalDate.of(2024, 1, 15),
                        BovineStatus.ACTIVE,
                        "TAG-042",
                        "REG-042",
                        "ABC",
                        BovineRegistrationCategory.PO,
                        new BovineId(10L),
                        new BovineId(11L),
                        null,
                        null
                ),
                createdAt,
                testOrigin()
        );

        BovineJpaEntity entity = BovineJpaMapper.toEntity(bovine);

        assertThat(entity.getId()).isEqualTo(42L);
        assertThat(entity.getInternalCode()).isEqualTo("BVL-26-0042");
        assertThat(entity.getName()).isEqualTo("Mimosa");
        assertThat(entity.getSex()).isEqualTo(BovineSex.FEMALE);
        assertThat(entity.getBreed()).isEqualTo("Nelore");
        assertThat(entity.getCoatColor()).isEqualTo("Branca");
        assertThat(entity.getBirthDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(entity.getStatus()).isEqualTo(BovineStatus.ACTIVE);
        assertThat(entity.getEarTag()).isEqualTo("TAG-042");
        assertThat(entity.getRegistrationNumber()).isEqualTo("REG-042");
        assertThat(entity.getRegistrationAssociation()).isEqualTo("ABC");
        assertThat(entity.getRegistrationCategory()).isEqualTo(BovineRegistrationCategory.PO);
        assertThat(entity.getFatherId()).isEqualTo(10L);
        assertThat(entity.getMotherId()).isEqualTo(11L);
        assertThat(entity.getFatherRegistrationNumber()).isNull();
        assertThat(entity.getMotherRegistrationNumber()).isNull();
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getUpdatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldMapOptionalParentIdsAndRegistrationNumbersAsNullWhenAbsent() {
        Bovine bovine = Bovine.createManually(
                new BovineId(43L),
                new BovineInternalCode("BVL-26-0043"),
                new BovineRegistrationData(
                        "Novilha",
                        BovineSex.FEMALE,
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
                ),
                Instant.parse("2026-06-01T12:00:00Z"),
                testOrigin()
        );

        BovineJpaEntity entity = BovineJpaMapper.toEntity(bovine);

        assertThat(entity.getFatherId()).isNull();
        assertThat(entity.getMotherId()).isNull();
        assertThat(entity.getRegistrationNumber()).isNull();
        assertThat(entity.getRegistrationAssociation()).isNull();
        assertThat(entity.getFatherRegistrationNumber()).isNull();
        assertThat(entity.getMotherRegistrationNumber()).isNull();
        assertThat(entity.getOrigin().getCreatorName()).isEqualTo("Criador de teste");
        assertThat(entity.getOrigin().getOriginFarmName()).isEqualTo("Fazenda de teste");
        assertThat(entity.getOrigin().getOriginFarmId()).isNull();
    }

    @Test
    void shouldMapLocalOriginFarmIdToDatabaseIdentifier() {
        Bovine bovine = Bovine.createManually(
                new BovineId(44L),
                new BovineInternalCode("BVL-26-0044"),
                new BovineRegistrationData(
                        "Novilha", BovineSex.FEMALE, null, null, null, BovineStatus.ACTIVE,
                        null, null, null, BovineRegistrationCategory.UNREGISTERED,
                        null, null, null, null
                ),
                Instant.parse("2026-06-01T12:00:00Z"),
                new BovineOrigin("Criador local", "Fazenda local", new FarmId(15L))
        );

        BovineJpaEntity entity = BovineJpaMapper.toEntity(bovine);

        assertThat(entity.getOrigin().getOriginFarmId()).isEqualTo(15L);
    }

    @Test
    void shouldMapExternalParentReferencesWithoutLocalParentIds() {
        Bovine bovine = Bovine.createManually(
                new BovineId(47L),
                new BovineInternalCode("BVL-26-0047"),
                new BovineRegistrationData(
                        "Jurema",
                        BovineSex.FEMALE,
                        "Nelore",
                        "Parda",
                        LocalDate.of(2024, 3, 12),
                        BovineStatus.ACTIVE,
                        "TAG-047",
                        "REG-047",
                        "ABC",
                        BovineRegistrationCategory.PO,
                        null,
                        null,
                        "EXT-PAI-047",
                        "EXT-MAE-047"
                ),
                Instant.parse("2026-06-01T12:00:00Z"),
                testOrigin()
        );

        BovineJpaEntity entity = BovineJpaMapper.toEntity(bovine);

        assertThat(entity.getFatherId()).isNull();
        assertThat(entity.getMotherId()).isNull();
        assertThat(entity.getFatherRegistrationNumber()).isEqualTo("EXT-PAI-047");
        assertThat(entity.getMotherRegistrationNumber()).isEqualTo("EXT-MAE-047");
    }

    @Test
    void shouldMapJpaEntityBackToDomainPreservingAllAttributesAndTimestamps() {
        Instant createdAt = Instant.parse("2026-06-01T12:00:00Z");
        Instant updatedAt = Instant.parse("2026-06-03T15:30:00Z");
        Bovine original = Bovine.reconstitute(
                new BovineId(45L),
                new BovineInternalCode("BVL-26-0045"),
                new BovineRegistrationData(
                        "Estrela",
                        BovineSex.FEMALE,
                        "Nelore",
                        "Branca",
                        LocalDate.of(2024, 2, 10),
                        BovineStatus.ACTIVE,
                        "TAG-045",
                        "REG-045",
                        "ABC",
                        BovineRegistrationCategory.PO,
                        new BovineId(10L),
                        new BovineId(11L),
                        null,
                        null
                ),
                createdAt,
                updatedAt,
                new BovineOrigin("Criador local", "Fazenda local", new FarmId(15L))
        );

        Bovine restored = BovineJpaMapper.toDomain(BovineJpaMapper.toEntity(original));

        assertThat(restored.id()).isEqualTo(original.id());
        assertThat(restored.internalCode()).isEqualTo(original.internalCode());
        assertThat(restored.name()).isEqualTo(original.name());
        assertThat(restored.sex()).isEqualTo(original.sex());
        assertThat(restored.breed()).isEqualTo(original.breed());
        assertThat(restored.coatColor()).isEqualTo(original.coatColor());
        assertThat(restored.birthDate()).isEqualTo(original.birthDate());
        assertThat(restored.status()).isEqualTo(original.status());
        assertThat(restored.earTag()).isEqualTo(original.earTag());
        assertThat(restored.registrationNumber()).isEqualTo(original.registrationNumber());
        assertThat(restored.registrationAssociation()).isEqualTo(original.registrationAssociation());
        assertThat(restored.registrationCategory()).isEqualTo(original.registrationCategory());
        assertThat(restored.fatherId()).isEqualTo(original.fatherId());
        assertThat(restored.motherId()).isEqualTo(original.motherId());
        assertThat(restored.fatherRegistrationNumber()).isEqualTo(original.fatherRegistrationNumber());
        assertThat(restored.motherRegistrationNumber()).isEqualTo(original.motherRegistrationNumber());
        assertThat(restored.origin()).isEqualTo(original.origin());
        assertThat(restored.createdAt()).isEqualTo(createdAt);
        assertThat(restored.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void shouldReconstructDomainFromJpaEntityCreatedDirectly() {
        Instant createdAt = Instant.parse("2026-06-01T12:00:00Z");
        Instant updatedAt = Instant.parse("2026-06-04T09:45:00Z");
        BovineJpaEntity entity = new BovineJpaEntity(
                48L,
                "BVL-26-0048",
                "Jurema",
                BovineSex.FEMALE,
                null,
                "Parda",
                null,
                BovineStatus.ACTIVE,
                null,
                null,
                null,
                BovineRegistrationCategory.UNREGISTERED,
                null,
                null,
                null,
                null,
                createdAt,
                updatedAt,
                new BovineOriginEmbeddable(
                        "Criador local",
                        "Fazenda local",
                        15L
                )
        );

        Bovine bovine = BovineJpaMapper.toDomain(entity);

        assertThat(bovine.id()).isEqualTo(new BovineId(48L));
        assertThat(bovine.internalCode()).isEqualTo(new BovineInternalCode("BVL-26-0048"));
        assertThat(bovine.name()).isEqualTo("Jurema");
        assertThat(bovine.sex()).isEqualTo(BovineSex.FEMALE);
        assertThat(bovine.breed()).isNull();
        assertThat(bovine.coatColor()).isEqualTo("Parda");
        assertThat(bovine.birthDate()).isNull();
        assertThat(bovine.status()).isEqualTo(BovineStatus.ACTIVE);
        assertThat(bovine.earTag()).isNull();
        assertThat(bovine.registrationNumber()).isNull();
        assertThat(bovine.registrationAssociation()).isNull();
        assertThat(bovine.registrationCategory()).isEqualTo(BovineRegistrationCategory.UNREGISTERED);
        assertThat(bovine.fatherId()).isNull();
        assertThat(bovine.motherId()).isNull();
        assertThat(bovine.fatherRegistrationNumber()).isNull();
        assertThat(bovine.motherRegistrationNumber()).isNull();
        assertThat(bovine.origin()).isEqualTo(
                new BovineOrigin("Criador local", "Fazenda local", new FarmId(15L))
        );
        assertThat(bovine.createdAt()).isEqualTo(createdAt);
        assertThat(bovine.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void shouldPreserveNullOptionalValuesWhenMappingJpaEntityToDomain() {
        Bovine original = Bovine.createManually(
                new BovineId(46L),
                new BovineInternalCode("BVL-26-0046"),
                new BovineRegistrationData(
                        "Novilha",
                        BovineSex.FEMALE,
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
                ),
                Instant.parse("2026-06-01T12:00:00Z"),
                testOrigin()
        );

        Bovine restored = BovineJpaMapper.toDomain(BovineJpaMapper.toEntity(original));

        assertThat(restored.breed()).isNull();
        assertThat(restored.coatColor()).isNull();
        assertThat(restored.birthDate()).isNull();
        assertThat(restored.earTag()).isNull();
        assertThat(restored.registrationNumber()).isNull();
        assertThat(restored.registrationAssociation()).isNull();
        assertThat(restored.fatherId()).isNull();
        assertThat(restored.motherId()).isNull();
        assertThat(restored.fatherRegistrationNumber()).isNull();
        assertThat(restored.motherRegistrationNumber()).isNull();
        assertThat(restored.origin().originFarmId()).isNull();
    }

    @Test
    void shouldRejectNullJpaEntityWhenMappingToDomain() {
        assertThatNullPointerException()
                .isThrownBy(() -> BovineJpaMapper.toDomain(null))
                .withMessage("A entidade JPA do bovino é obrigatória");
    }

    @Test
    void shouldRejectNullPersistedOriginWhenMappingToDomain() {
        BovineJpaEntity entity = new BovineJpaEntity(
                49L,
                "BVL-26-0049",
                "Jurema",
                BovineSex.FEMALE,
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
                null,
                Instant.parse("2026-06-01T12:00:00Z"),
                Instant.parse("2026-06-01T12:00:00Z"),
                null
        );

        assertThatNullPointerException()
                .isThrownBy(() -> BovineJpaMapper.toDomain(entity))
                .withMessage("A origem persistida do bovino é obrigatória");
    }

    @Test
    void shouldRejectNullBovine() {
        assertThatNullPointerException()
                .isThrownBy(() -> BovineJpaMapper.toEntity(null))
                .withMessage("O bovino é obrigatório");
    }

    private static BovineOrigin testOrigin() {
        return new BovineOrigin("Criador de teste", "Fazenda de teste", null);
    }
}
