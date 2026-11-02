package com.bovigestor.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BovineOriginTest {

    @Test
    void shouldPreserveExternalOriginWithoutLocalFarmId() {
        BovineOrigin origin = new BovineOrigin(
                "José Pereira",
                "Fazenda Santa Rita",
                null
        );

        assertThat(origin.creatorName()).isEqualTo("José Pereira");
        assertThat(origin.originFarmName()).isEqualTo("Fazenda Santa Rita");
        assertThat(origin.originFarmId()).isNull();
    }

    @Test
    void shouldPreserveLocalOriginFarmId() {
        FarmId farmId = new FarmId(1L);

        BovineOrigin origin = new BovineOrigin(
                "José Pereira",
                "Fazenda Santa Rita",
                farmId
        );

        assertThat(origin.originFarmId()).isEqualTo(farmId);
    }

    @Test
    void shouldRejectNullCreatorName() {
        assertThatThrownBy(() -> new BovineOrigin(
                null,
                "Fazenda Santa Rita",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome do criador é obrigatório");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\n"})
    void shouldRejectBlankCreatorName(String creatorName) {
        assertThatThrownBy(() -> new BovineOrigin(
                creatorName,
                "Fazenda Santa Rita",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome do criador é obrigatório");
    }

    @Test
    void shouldRejectNullOriginFarmName() {
        assertThatThrownBy(() -> new BovineOrigin(
                "José Pereira",
                null,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome da fazenda de origem é obrigatório");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\n"})
    void shouldRejectBlankOriginFarmName(String originFarmName) {
        assertThatThrownBy(() -> new BovineOrigin(
                "José Pereira",
                originFarmName,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome da fazenda de origem é obrigatório");
    }

    @Test
    void shouldStripWhitespaceFromNames() {
        BovineOrigin origin = new BovineOrigin(
                "  José Pereira  ",
                "  Fazenda Santa Rita  ",
                null
        );

        assertThat(origin.creatorName()).isEqualTo("José Pereira");
        assertThat(origin.originFarmName()).isEqualTo("Fazenda Santa Rita");
    }

    @Test
    void shouldRejectCreatorNameLongerThan255Characters() {
        assertThatThrownBy(() -> new BovineOrigin(
                "C".repeat(256),
                "Fazenda Santa Rita",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome do criador deve conter no máximo 255 caracteres");
    }

    @Test
    void shouldRejectOriginFarmNameLongerThan255Characters() {
        assertThatThrownBy(() -> new BovineOrigin(
                "José Pereira",
                "F".repeat(256),
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome da fazenda de origem deve conter no máximo 255 caracteres");
    }

    @Test
    void shouldAcceptNamesAtMaximumLength() {
        String creatorName = "C".repeat(255);
        String originFarmName = "F".repeat(255);

        BovineOrigin origin = new BovineOrigin(creatorName, originFarmName, null);

        assertThat(origin.creatorName()).hasSize(255);
        assertThat(origin.originFarmName()).hasSize(255);
    }

    @Test
    void shouldBeEqualWhenOriginsHaveSameValues() {
        BovineOrigin origin1 = new BovineOrigin(
                "José Pereira",
                "Fazenda Santa Rita",
                null
        );

        BovineOrigin origin2 = new BovineOrigin(
                "José Pereira",
                "Fazenda Santa Rita",
                null
        );

        assertThat(origin1).isEqualTo(origin2);
    }

    @Test
    void shouldNotBeEqualWhenOriginFarmNamesDiffer() {
        BovineOrigin origin1 = new BovineOrigin(
                "José Pereira",
                "Fazenda Santa Rita",
                null
        );

        BovineOrigin origin2 = new BovineOrigin(
                "José Pereira",
                "Fazenda Boa Esperança",
                null
        );

        assertThat(origin1).isNotEqualTo(origin2);
    }

    @Test
    void shouldBeEqualAfterNameNormalization() {
        BovineOrigin origin1 = new BovineOrigin(
                " José Pereira ",
                " Fazenda Santa Rita ",
                null
        );

        BovineOrigin origin2 = new BovineOrigin(
                "José Pereira",
                "Fazenda Santa Rita",
                null
        );

        assertThat(origin1).isEqualTo(origin2);
    }
}
