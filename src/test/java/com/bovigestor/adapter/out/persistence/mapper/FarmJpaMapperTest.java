package com.bovigestor.adapter.out.persistence.mapper;

import com.bovigestor.adapter.out.persistence.entity.FarmJpaEntity;
import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;
import com.bovigestor.domain.model.FarmStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class FarmJpaMapperTest {

    @Test
    void shouldMapAllAttributesAndPreserveDistinctTimestamps() {
        FarmId id = new FarmId(1L);
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T10:00:00Z");
        ZoneId timeZone = ZoneId.of("America/Sao_Paulo");
        Farm farm = Farm.reconstitute(
                id,
                "Fazenda Boa Vista",
                "BVL",
                timeZone,
                FarmStatus.INACTIVE,
                createdAt,
                updatedAt
        );

        FarmJpaEntity entity = FarmJpaMapper.toEntity(farm);

        assertThat(entity.getId()).isEqualTo(id.value());
        assertThat(entity.getName()).isEqualTo("Fazenda Boa Vista");
        assertThat(entity.getCodeSeries()).isEqualTo("BVL");
        assertThat(entity.getTimeZone()).isEqualTo(timeZone.getId());
        assertThat(entity.getStatus()).isEqualTo(FarmStatus.INACTIVE);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void shouldReconstituteFarmUsingPersistedTimeZoneAndTimestamps() {
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T10:00:00Z");
        FarmJpaEntity entity = new FarmJpaEntity(
                12L,
                "Fazenda Santa Rita",
                "SRT",
                "America/Manaus",
                FarmStatus.INACTIVE,
                createdAt,
                updatedAt
        );

        Farm farm = FarmJpaMapper.toDomain(entity);

        assertThat(farm.id()).isEqualTo(new FarmId(12L));
        assertThat(farm.name()).isEqualTo("Fazenda Santa Rita");
        assertThat(farm.codeSeries()).isEqualTo("SRT");
        assertThat(farm.timeZone()).isEqualTo(ZoneId.of("America/Manaus"));
        assertThat(farm.status()).isEqualTo(FarmStatus.INACTIVE);
        assertThat(farm.createdAt()).isEqualTo(createdAt);
        assertThat(farm.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void shouldRejectNullJpaEntityWhenMappingToDomain() {
        assertThatNullPointerException()
                .isThrownBy(() -> FarmJpaMapper.toDomain(null))
                .withMessage("A entidade JPA da fazenda é obrigatória");
    }

    @Test
    void shouldRejectLegacyEntityWithoutPersistedTimeZone() {
        FarmJpaEntity legacyEntity = new FarmJpaEntity(
                13L,
                "Fazenda Legada",
                "LEG",
                null,
                FarmStatus.ACTIVE,
                Instant.parse("2020-01-01T10:00:00Z"),
                Instant.parse("2020-01-01T10:00:00Z")
        );

        assertThatNullPointerException()
                .isThrownBy(() -> FarmJpaMapper.toDomain(legacyEntity))
                .withMessage("O fuso horário persistido da fazenda é obrigatório");
    }
}
