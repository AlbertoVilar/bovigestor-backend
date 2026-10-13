package com.bovigestor.domain.model;

import java.util.Objects;

public record FarmId(Long value) {

    public FarmId {
        Objects.requireNonNull(value, "O identificador da fazenda é obrigatório");

        if (value <= 0) {
            throw new IllegalArgumentException("O identificador da fazenda deve ser positivo");
        }
    }
}
