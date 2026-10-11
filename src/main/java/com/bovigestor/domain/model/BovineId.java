package com.bovigestor.domain.model;

import java.util.Objects;

public record BovineId(Long value) {

    public BovineId {
        Objects.requireNonNull(value, "O identificador é obrigatório");
        if (value <= 0) {
            throw new IllegalArgumentException("O identificador deve ser positivo");
        }
    }

}