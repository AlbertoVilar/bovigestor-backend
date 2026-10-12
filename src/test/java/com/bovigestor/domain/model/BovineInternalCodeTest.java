package com.bovigestor.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BovineInternalCodeTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "BVL-26-0001",
            "BVL-26-00001",
            "AVIL-26-0098",
            "BVL-27-10000"
    })
    void shouldCreateValidInternalCode(String value) {
        // Act
        BovineInternalCode code = new BovineInternalCode(value);

        // Assert
        assertThat(code.value()).isEqualTo(value);
    }

    @Test
    void shouldBeEqualWhenInternalCodesHaveSameValue() {
        BovineInternalCode code1 =
                new BovineInternalCode("BVL-26-0001");

        BovineInternalCode code2 =
                new BovineInternalCode("BVL-26-0001");

        assertThat(code1).isEqualTo(code2);
        assertThat(code1.hashCode()).isEqualTo(code2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenInternalCodesHaveDifferentValues() {
        // Arrange
        BovineInternalCode code1 =
                new BovineInternalCode("BVL-26-0001");
        BovineInternalCode code2 =
                new BovineInternalCode("BVL-26-0002");

        // Act & Assert
        assertThat(code1).isNotEqualTo(code2);
    }

    @Test
    void shouldRejectNullInternalCode() {
        // Arrange
        String value = null;

        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new BovineInternalCode(value)
        );

        assertThat(exception.getMessage())
                .isEqualTo("O código interno do bovino é obrigatório");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "BVL-26-0000",
            "BVL-26-00000",
            "BVL-2026-0001",
            "BV-26-0001",
            "BVL-26-001",
            "BVL-26-00A1",
            "bvl-26-0001",
            " BVL-26-0001",
            "BVL-26-0001 ",
            "BVL_26_0001"
    })
    void shouldRejectInvalidInternalCode(String value) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BovineInternalCode(value)
        );

        assertThat(exception.getMessage())
                .isEqualTo(
                        "O código interno deve seguir o formato SERIE-AA-SEQUENCIA, "
                                + "com série de 3 a 4 letras maiúsculas e sequência positiva de pelo menos quatro dígitos"
                );
    }
}
