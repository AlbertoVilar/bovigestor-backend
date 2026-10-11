package com.bovigestor.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BovineIdTest {

    @Test
    void shouldCreateBovineIdWithPositiveValue() {
        // Arrange
        Long id = 1L;
        // Act
        var bovineId = new BovineId(id);
        // Assert
        assertThat(bovineId.value())
                .isEqualTo(id);
    }

    @Test
    void shouldRejectNullBovineId() {
        // Arrange
        Long id = null;

        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new BovineId(id)
        );

        assertEquals("O identificador é obrigatório", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void shouldRejectNonPositiveBovineId(long id) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BovineId(id)
        );

        assertEquals(
                "O identificador deve ser positivo",
                exception.getMessage()
        );
    }

    @Test
    void shouldConsiderBovineIdsWithSameValueEqual() {
        // Arrange
        BovineId firstBovineId = new BovineId(1L);
        BovineId secondBovineId = new BovineId(1L);

        // Assert
        assertThat(firstBovineId).isEqualTo(secondBovineId);
    }

    @Test
    void shouldConsiderBovineIdsWithDifferentValuesNotEqual() {
        // Arrange
        BovineId firstBovineId = new BovineId(1L);
        BovineId secondBovineId = new BovineId(2L);

        // Act & Assert
        assertThat(firstBovineId).isNotEqualTo(secondBovineId);
    }

    @Test
    void shouldGenerateSameHashCodeForEqualBovineIds() {
        // Arrange
        BovineId firstBovineId = new BovineId(1L);
        BovineId secondBovineId = new BovineId(1L);

        // Act & Assert
        assertThat(firstBovineId.hashCode())
                .isEqualTo(secondBovineId.hashCode());
    }
}
