package com.bovigestor.domain.model;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FarmTest {

    private static final FarmId FARM_ID = new FarmId(1L);
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void shouldCreateFarmWithActiveStatusAndInitialTimestamps() {
        Farm farm = createFarm();

        assertThat(farm.id()).isEqualTo(FARM_ID);
        assertThat(farm.name()).isEqualTo("Fazenda Boa Vista");
        assertThat(farm.codeSeries()).isEqualTo("BVL");
        assertThat(farm.status()).isEqualTo(FarmStatus.ACTIVE);
        assertThat(farm.createdAt()).isEqualTo(CREATED_AT);
        assertThat(farm.updatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void shouldRejectNullFarmId() {
        assertThatThrownBy(() -> Farm.create(null, "Fazenda Boa Vista", "BVL", CREATED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O identificador da fazenda é obrigatório");
    }

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() -> Farm.create(FARM_ID, null, "BVL", CREATED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O nome da fazenda é obrigatório");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void shouldRejectBlankName(String name) {
        assertThatThrownBy(() -> Farm.create(FARM_ID, name, "BVL", CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome da fazenda não pode estar vazio ou conter somente espaços");
    }

    @Test
    void shouldRejectNullCodeSeries() {
        assertThatThrownBy(() -> Farm.create(FARM_ID, "Fazenda Boa Vista", null, CREATED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("A série da fazenda é obrigatória");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "bv", "BVL01", "ABCDE"})
    void shouldRejectInvalidCodeSeries(String codeSeries) {
        assertThatThrownBy(() -> Farm.create(FARM_ID, "Fazenda Boa Vista", codeSeries, CREATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A série da fazenda deve conter três ou quatro letras maiúsculas");
    }

    @Test
    void shouldAcceptFourLetterCodeSeries() {
        Farm farm = Farm.create(FARM_ID, "Fazenda Boa Vista", "BOAS", CREATED_AT);

        assertThat(farm.codeSeries()).isEqualTo("BOAS");
    }

    @Test
    void shouldRejectNullCreatedAt() {
        assertThatThrownBy(() -> Farm.create(FARM_ID, "Fazenda Boa Vista", "BVL", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("A data de criação é obrigatória");
    }

    @Test
    void shouldDeactivateActiveFarmAndUpdateTimestamp() {
        Farm farm = createFarm();
        Instant occurredAt = CREATED_AT.plusSeconds(60);

        farm.deactivate(occurredAt);

        assertThat(farm.status()).isEqualTo(FarmStatus.INACTIVE);
        assertThat(farm.updatedAt()).isEqualTo(occurredAt);
        assertThat(farm.id()).isEqualTo(FARM_ID);
        assertThat(farm.createdAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void shouldReactivateInactiveFarmAndUpdateTimestamp() {
        Farm farm = createFarm();
        Instant deactivatedAt = CREATED_AT.plusSeconds(60);
        Instant activatedAt = deactivatedAt.plusSeconds(60);
        farm.deactivate(deactivatedAt);

        farm.activate(activatedAt);

        assertThat(farm.status()).isEqualTo(FarmStatus.ACTIVE);
        assertThat(farm.updatedAt()).isEqualTo(activatedAt);
        assertThat(farm.id()).isEqualTo(FARM_ID);
        assertThat(farm.createdAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void shouldBeIdempotentWhenActivatingActiveFarm() {
        Farm farm = createFarm();

        farm.activate(null);

        assertThat(farm.status()).isEqualTo(FarmStatus.ACTIVE);
        assertThat(farm.updatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void shouldBeIdempotentWhenDeactivatingInactiveFarm() {
        Farm farm = createFarm();
        Instant deactivatedAt = CREATED_AT.plusSeconds(60);
        farm.deactivate(deactivatedAt);

        farm.deactivate(null);

        assertThat(farm.status()).isEqualTo(FarmStatus.INACTIVE);
        assertThat(farm.updatedAt()).isEqualTo(deactivatedAt);
    }

    @Test
    void shouldRejectNullOccurredAtForEffectiveStatusChange() {
        Farm farm = createFarm();

        assertThatThrownBy(() -> farm.deactivate(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("A data da alteração é obrigatória");
    }

    @Test
    void shouldRejectOccurredAtBeforeCreatedAt() {
        Farm farm = createFarm();

        assertThatThrownBy(() -> farm.deactivate(CREATED_AT.minusNanos(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "A data da alteração não pode ser anterior à criação ou à última atualização"
                );
    }

    @Test
    void shouldRejectOccurredAtBeforeUpdatedAt() {
        Farm farm = createFarm();
        farm.deactivate(CREATED_AT.plusSeconds(60));

        assertThatThrownBy(() -> farm.activate(CREATED_AT.plusSeconds(30)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "A data da alteração não pode ser anterior à criação ou à última atualização"
                );
    }

    @Test
    void shouldAllowOccurredAtEqualToUpdatedAt() {
        Farm farm = createFarm();
        Instant occurredAt = CREATED_AT.plusSeconds(60);
        farm.deactivate(occurredAt);

        farm.activate(occurredAt);

        assertThat(farm.status()).isEqualTo(FarmStatus.ACTIVE);
        assertThat(farm.updatedAt()).isEqualTo(occurredAt);
    }

    private static Farm createFarm() {
        return Farm.create(FARM_ID, "Fazenda Boa Vista", "BVL", CREATED_AT);
    }
}
