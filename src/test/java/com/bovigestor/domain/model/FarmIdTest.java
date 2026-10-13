package com.bovigestor.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FarmIdTest {

    @Test
    void shouldCreateFarmIdWithPositiveValue() {
        FarmId farmId = new FarmId(1L);

        assertThat(farmId.value()).isEqualTo(1L);
    }

    @Test
    void shouldRejectNullFarmIdValue() {
        assertThatThrownBy(() -> new FarmId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O identificador da fazenda é obrigatório");
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void shouldRejectNonPositiveFarmIdValue(long value) {
        assertThatThrownBy(() -> new FarmId(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O identificador da fazenda deve ser positivo");
    }

    @Test
    void shouldConsiderFarmIdsWithSameValueEqual() {
        FarmId firstFarmId = new FarmId(1L);
        FarmId secondFarmId = new FarmId(1L);

        assertThat(firstFarmId).isEqualTo(secondFarmId);
    }
}
