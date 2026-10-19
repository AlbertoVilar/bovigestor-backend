package com.bovigestor.adapter.out.persistence.mapper;

import com.bovigestor.adapter.out.persistence.entity.BovineJpaEntity;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineInternalCode;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineRegistrationData;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
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
                createdAt
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
                Instant.parse("2026-06-01T12:00:00Z")
        );

        BovineJpaEntity entity = BovineJpaMapper.toEntity(bovine);

        assertThat(entity.getFatherId()).isNull();
        assertThat(entity.getMotherId()).isNull();
        assertThat(entity.getRegistrationNumber()).isNull();
        assertThat(entity.getRegistrationAssociation()).isNull();
        assertThat(entity.getFatherRegistrationNumber()).isNull();
        assertThat(entity.getMotherRegistrationNumber()).isNull();
    }

    @Test
    void shouldRejectNullBovine() {
        assertThatNullPointerException()
                .isThrownBy(() -> BovineJpaMapper.toEntity(null))
                .withMessage("O bovino é obrigatório");
    }
}
