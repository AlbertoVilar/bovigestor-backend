package com.bovigestor.adapter.out.persistence.mapper;

import com.bovigestor.adapter.out.persistence.entity.BovineOwnershipPeriodJpaEntity;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineOwnershipPeriod;
import com.bovigestor.domain.model.BovineOwnershipPeriodId;
import com.bovigestor.domain.model.FarmId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class BovineOwnershipPeriodJpaMapperTest {

    @Test
    void shouldMapAllOwnershipPeriodAttributesToJpaEntity() {
        // Arrange
        BovineOwnershipPeriodId id = new BovineOwnershipPeriodId(42L);
        BovineId bovineId = new BovineId(7L);
        FarmId farmId = new FarmId(3L);
        Instant startedAt = Instant.parse("2026-06-01T12:00:00Z");
        Instant endedAt = Instant.parse("2026-06-15T12:00:00Z");

        BovineOwnershipPeriod period = BovineOwnershipPeriod
                .open(id, bovineId, farmId, startedAt)
                .closeAt(endedAt);

        // Act
        var entity = BovineOwnershipPeriodJpaMapper.toEntity(period);

        // Assert
        assertThat(entity.getId()).isEqualTo(id.value());
        assertThat(entity.getBovineId()).isEqualTo(bovineId.value());
        assertThat(entity.getFarmId()).isEqualTo(farmId.value());
        assertThat(entity.getStartedAt()).isEqualTo(startedAt);
        assertThat(entity.getEndedAt()).isEqualTo(endedAt);
    }

    @Test
    void shouldMapOpenOwnershipPeriodWithNullEndedAt() {
        // Arrange
        BovineOwnershipPeriodId id = new BovineOwnershipPeriodId(43L);
        BovineId bovineId = new BovineId(8L);
        FarmId farmId = new FarmId(4L);
        Instant startedAt = Instant.parse("2026-07-01T09:30:00Z");

        BovineOwnershipPeriod period = BovineOwnershipPeriod
                .open(id, bovineId, farmId, startedAt);

        // Act
        var entity = BovineOwnershipPeriodJpaMapper.toEntity(period);

        // Assert
        assertThat(entity.getId()).isEqualTo(id.value());
        assertThat(entity.getBovineId()).isEqualTo(bovineId.value());
        assertThat(entity.getFarmId()).isEqualTo(farmId.value());
        assertThat(entity.getStartedAt()).isEqualTo(startedAt);
        assertThat(entity.getEndedAt()).isNull();
    }

    @Test
    void shouldRejectNullOwnershipPeriod() {
        // Arrange
        BovineOwnershipPeriod period = null;

        // Act and Assert
        assertThatNullPointerException()
                .isThrownBy(() -> BovineOwnershipPeriodJpaMapper.toEntity(period))
                .withMessage("O período de custódia é obrigatório");
    }

    @Test
    void shouldMapAllJpaEntityAttributesToClosedOwnershipPeriod() {
        // Arrange
        Long id = 42L;
        Long bovineId = 7L;
        Long farmId = 3L;
        Instant startedAt = Instant.parse("2026-06-01T12:00:00Z");
        Instant endedAt = Instant.parse("2026-06-15T12:00:00Z");
        BovineOwnershipPeriodJpaEntity entity = new BovineOwnershipPeriodJpaEntity(
                id, bovineId, farmId, startedAt, endedAt
        );

        // Act
        BovineOwnershipPeriod period = BovineOwnershipPeriodJpaMapper.toDomain(entity);

        // Assert
        assertThat(period.id()).isEqualTo(new BovineOwnershipPeriodId(id));
        assertThat(period.bovineId()).isEqualTo(new BovineId(bovineId));
        assertThat(period.farmId()).isEqualTo(new FarmId(farmId));
        assertThat(period.startedAt()).isEqualTo(startedAt);
        assertThat(period.endedAt()).isEqualTo(endedAt);
    }

    @Test
    void shouldMapOpenJpaEntityToOwnershipPeriodWithNullEnd() {
        // Arrange
        Long id = 43L;
        Long bovineId = 8L;
        Long farmId = 4L;
        Instant startedAt = Instant.parse("2026-07-01T09:30:00Z");
        BovineOwnershipPeriodJpaEntity entity = new BovineOwnershipPeriodJpaEntity(
                id, bovineId, farmId, startedAt, null
        );

        // Act
        BovineOwnershipPeriod period = BovineOwnershipPeriodJpaMapper.toDomain(entity);

        // Assert
        assertThat(period.id()).isEqualTo(new BovineOwnershipPeriodId(id));
        assertThat(period.bovineId()).isEqualTo(new BovineId(bovineId));
        assertThat(period.farmId()).isEqualTo(new FarmId(farmId));
        assertThat(period.startedAt()).isEqualTo(startedAt);
        assertThat(period.endedAt()).isNull();
    }

    @Test
    void shouldRejectNullJpaEntity() {
        // Arrange
        BovineOwnershipPeriodJpaEntity entity = null;

        // Act and Assert
        assertThatNullPointerException()
                .isThrownBy(() -> BovineOwnershipPeriodJpaMapper.toDomain(entity))
                .withMessage("A entidade do período de custódia é obrigatória");
    }
}
