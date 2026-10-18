package com.bovigestor.domain.model;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BovineOwnershipPeriodTest {

    @Test
    void shouldCreateBovineOwnershipPeriodIdWithPositiveValue() {
        // Arrange
        Long value = 1L;

        // Act
        var id = new BovineOwnershipPeriodId(value);

        // Assert
        assertThat(id.value()).isEqualTo(value);
    }

    @Test
    void shouldRejectNullBovineOwnershipPeriodIdValue() {
        // Arrange
        Long value = null;

        // Act & Assert
        assertThatThrownBy(() -> new BovineOwnershipPeriodId(value))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O identificador do período é obrigatório");
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void shouldRejectNonPositiveBovineOwnershipPeriodIdValue(long value) {
        // Act & Assert
        assertThatThrownBy(() -> new BovineOwnershipPeriodId(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O identificador do período deve ser positivo");
    }

    @Test
    void shouldConsiderBovineOwnershipPeriodIdsWithSameValueEqual() {
        // Arrange
        BovineOwnershipPeriodId firstId = new BovineOwnershipPeriodId(1L);
        BovineOwnershipPeriodId secondId = new BovineOwnershipPeriodId(1L);

        // Act & Assert
        assertThat(firstId).isEqualTo(secondId);
    }

    @Test
    void shouldConsiderBovineOwnershipPeriodIdsWithDifferentValuesNotEqual() {
        // Arrange
        BovineOwnershipPeriodId firstId = new BovineOwnershipPeriodId(1L);
        BovineOwnershipPeriodId secondId = new BovineOwnershipPeriodId(2L);

        // Act & Assert
        assertThat(firstId).isNotEqualTo(secondId);
    }

    @Test
    void shouldGenerateSameHashCodeForEqualBovineOwnershipPeriodIds() {
        // Arrange
        BovineOwnershipPeriodId firstId = new BovineOwnershipPeriodId(1L);
        BovineOwnershipPeriodId secondId = new BovineOwnershipPeriodId(1L);

        // Act & Assert
        assertThat(firstId.hashCode()).isEqualTo(secondId.hashCode());
    }

    @Test
    void shouldOpenOwnershipPeriod() {
        // Arrange
        BovineOwnershipPeriodId id = new BovineOwnershipPeriodId(1L);
        BovineId bovineId = new BovineId(1L);
        FarmId farmId = new FarmId(1L);
        Instant startedAt = Instant.parse("2026-01-01T00:00:00Z");

        // Act
        var data = BovineOwnershipPeriod.open(
                id, bovineId, farmId, startedAt
        );

        // Assert
        assertThat(data)
                .extracting(
                        BovineOwnershipPeriod::id,
                        BovineOwnershipPeriod::bovineId,
                        BovineOwnershipPeriod::farmId,
                        BovineOwnershipPeriod::startedAt,
                        BovineOwnershipPeriod::endedAt
                )
                .containsExactly(id, bovineId, farmId, startedAt, null);
    }

    @Test
    void shouldRejectNullBovineId() {
        // Arrange
        BovineOwnershipPeriodId id = new BovineOwnershipPeriodId(1L);
        BovineId bovineId = null;
        FarmId farmId = new FarmId(1L);
        Instant startedAt = Instant.parse("2026-01-01T00:00:00Z");

        // Act
        assertThrows(NullPointerException.class, () -> BovineOwnershipPeriod.open(
                id, bovineId, farmId, startedAt
        ));
    }

    @Test
    void shouldRejectNullBovineOwnershipPeriodId() {
        // Arrange
        BovineOwnershipPeriodId id = null;
        BovineId bovineId = new BovineId(1L);
        FarmId farmId = new FarmId(1L);
        Instant startedAt = Instant.parse("2026-01-01T00:00:00Z");

        // Act & Assert
        assertThatThrownBy(() -> BovineOwnershipPeriod.open(
                id, bovineId, farmId, startedAt
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O identificador do período é obrigatório");
    }

    @Test
    void shouldRejectNullFarmId() {
        // Arrange
        BovineOwnershipPeriodId id = new BovineOwnershipPeriodId(1L);
        BovineId bovineId = new BovineId(1L);
        FarmId farmId = null;
        Instant startedAt = Instant.parse("2026-01-01T00:00:00Z");

        // Act & Assert
        assertThatThrownBy(() -> BovineOwnershipPeriod.open(
                id, bovineId, farmId, startedAt
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("A fazenda do período é obrigatória");
    }

    @Test
    void shouldRejectNullStartedAt() {
        // Arrange
        BovineOwnershipPeriodId id = new BovineOwnershipPeriodId(1L);
        BovineId bovineId = new BovineId(1L);
        FarmId farmId = new FarmId(1L);
        Instant startedAt = null;

        // Act & Assert
        assertThatThrownBy(() -> BovineOwnershipPeriod.open(
                id, bovineId, farmId, startedAt
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O início do período é obrigatório");
    }

    @Test
    void shouldContainInstantAtStart() {
        // Arrange
        BovineOwnershipPeriod period = BovineOwnershipPeriod.open(
                new BovineOwnershipPeriodId(1L),
                new BovineId(1L),
                new FarmId(1L),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");

        // Act
        boolean containsInstant = period.contains(instant);

        // Assert
        assertThat(containsInstant).isTrue();
    }

    @Test
    void shouldContainInstantWithinOpenPeriod() {
        // Arrange
        BovineOwnershipPeriod period = BovineOwnershipPeriod.open(
                new BovineOwnershipPeriodId(1L),
                new BovineId(1L),
                new FarmId(1L),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        Instant instant = Instant.parse("2026-01-15T00:00:00Z");

        // Act
        boolean containsInstant = period.contains(instant);

        // Assert
        assertThat(containsInstant).isTrue();
    }

    @Test
    void shouldRejectInstantBeforeStart() {
        // Arrange
        BovineOwnershipPeriod period = BovineOwnershipPeriod.open(
                new BovineOwnershipPeriodId(1L),
                new BovineId(1L),
                new FarmId(1L),
                Instant.parse("2026-01-02T00:00:00Z")
        );
        Instant instant = Instant.parse("2026-01-01T23:59:59Z");

        // Act
        boolean containsInstant = period.contains(instant);

        // Assert
        assertThat(containsInstant).isFalse();
    }

    @Test
    void shouldContainInstantBeforeEnd() {
        // Arrange
        BovineOwnershipPeriod period = BovineOwnershipPeriod.open(
                new BovineOwnershipPeriodId(1L),
                new BovineId(1L),
                new FarmId(1L),
                Instant.parse("2026-01-01T00:00:00Z")
        ).closeAt(Instant.parse("2026-02-01T00:00:00Z"));
        Instant instant = Instant.parse("2026-01-31T23:59:59Z");

        // Act
        boolean containsInstant = period.contains(instant);

        // Assert
        assertThat(containsInstant).isTrue();
    }

    @Test
    void shouldNotContainInstantAtEnd() {
        // Arrange
        Instant endedAt = Instant.parse("2026-02-01T00:00:00Z");
        BovineOwnershipPeriod period = BovineOwnershipPeriod.open(
                new BovineOwnershipPeriodId(1L),
                new BovineId(1L),
                new FarmId(1L),
                Instant.parse("2026-01-01T00:00:00Z")
        ).closeAt(endedAt);

        // Act
        boolean containsInstant = period.contains(endedAt);

        // Assert
        assertThat(containsInstant).isFalse();
    }

    @Test
    void shouldNotContainInstantAfterEnd() {
        // Arrange
        BovineOwnershipPeriod period = BovineOwnershipPeriod.open(
                new BovineOwnershipPeriodId(1L),
                new BovineId(1L),
                new FarmId(1L),
                Instant.parse("2026-01-01T00:00:00Z")
        ).closeAt(Instant.parse("2026-02-01T00:00:00Z"));
        Instant instant = Instant.parse("2026-02-01T00:00:01Z");

        // Act
        boolean containsInstant = period.contains(instant);

        // Assert
        assertThat(containsInstant).isFalse();
    }

    @Test
    void shouldRejectNullConsultedInstant() {
        // Arrange
        BovineOwnershipPeriod period = BovineOwnershipPeriod.open(
                new BovineOwnershipPeriodId(1L),
                new BovineId(1L),
                new FarmId(1L),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        Instant instant = null;

        // Act & Assert
        assertThatThrownBy(() -> period.contains(instant))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O instante consultado é obrigatório");
    }

    @Test
    void shouldCloseOwnershipPeriod() {
        // Arrange
        BovineOwnershipPeriodId id = new BovineOwnershipPeriodId(1L);
        BovineId bovineId = new BovineId(1L);
        FarmId farmId = new FarmId(1L);
        Instant startedAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endedAt = Instant.parse("2026-02-01T00:00:00Z");
        BovineOwnershipPeriod openPeriod = BovineOwnershipPeriod.open(
                id, bovineId, farmId, startedAt
        );

        // Act
        BovineOwnershipPeriod closedPeriod = openPeriod.closeAt(endedAt);

        // Assert
        assertThat(closedPeriod)
                .extracting(
                        BovineOwnershipPeriod::id,
                        BovineOwnershipPeriod::bovineId,
                        BovineOwnershipPeriod::farmId,
                        BovineOwnershipPeriod::startedAt,
                        BovineOwnershipPeriod::endedAt
                )
                .containsExactly(id, bovineId, farmId, startedAt, endedAt);
    }

    @Test
    void shouldPreserveOriginalPeriodWhenClosing() {
        // Arrange
        BovineOwnershipPeriodId id = new BovineOwnershipPeriodId(1L);
        BovineId bovineId = new BovineId(1L);
        FarmId farmId = new FarmId(1L);
        Instant startedAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endedAt = Instant.parse("2026-02-01T00:00:00Z");
        var originalPeriod = BovineOwnershipPeriod.open(
                id, bovineId, farmId, startedAt
        );

        // Act
        var closedPeriod = originalPeriod.closeAt(endedAt);

        // Assert
        assertThat(originalPeriod.endedAt()).isNull();
        assertThat(closedPeriod).isNotSameAs(originalPeriod);
        assertThat(closedPeriod.id()).isEqualTo(id);
        assertThat(closedPeriod.endedAt()).isEqualTo(endedAt);
    }

    @Test
    void shouldRejectClosingAlreadyClosedPeriod() {
        // Arrange
        BovineOwnershipPeriod period = BovineOwnershipPeriod.open(
                new BovineOwnershipPeriodId(1L),
                new BovineId(1L),
                new FarmId(1L),
                Instant.parse("2026-01-01T00:00:00Z")
        ).closeAt(Instant.parse("2026-02-01T00:00:00Z"));
        Instant secondEnd = Instant.parse("2026-03-01T00:00:00Z");

        // Act & Assert
        assertThatThrownBy(() -> period.closeAt(secondEnd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("O período já está encerrado");
    }

    @Test
    void shouldRejectNullClosingInstant() {
        // Arrange
        BovineOwnershipPeriod period = BovineOwnershipPeriod.open(
                new BovineOwnershipPeriodId(1L),
                new BovineId(1L),
                new FarmId(1L),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        Instant endedAt = null;

        // Act & Assert
        assertThatThrownBy(() -> period.closeAt(endedAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O instante de encerramento é obrigatório");
    }

    @Test
    void shouldRejectClosingAtStart() {
        // Arrange
        Instant startedAt = Instant.parse("2026-01-01T00:00:00Z");
        BovineOwnershipPeriod period = BovineOwnershipPeriod.open(
                new BovineOwnershipPeriodId(1L),
                new BovineId(1L),
                new FarmId(1L),
                startedAt
        );

        // Act & Assert
        assertThatThrownBy(() -> period.closeAt(startedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O fim do período deve ser posterior ao início");
    }

    @Test
    void shouldRejectClosingBeforeStart() {
        // Arrange
        Instant startedAt = Instant.parse("2026-01-02T00:00:00Z");
        BovineOwnershipPeriod period = BovineOwnershipPeriod.open(
                new BovineOwnershipPeriodId(1L),
                new BovineId(1L),
                new FarmId(1L),
                startedAt
        );
        Instant endedAt = Instant.parse("2026-01-01T23:59:59Z");

        // Act & Assert
        assertThatThrownBy(() -> period.closeAt(endedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O fim do período deve ser posterior ao início");
    }
}
