package com.bovigestor.domain.model;

import java.util.Objects;

public record BovineOwnershipPeriodId(Long value) {

    public BovineOwnershipPeriodId {
        Objects.requireNonNull(value, "O identificador do período é obrigatório");

        if (value <= 0) {
            throw new IllegalArgumentException(
                    "O identificador do período deve ser positivo"
            );
        }
    }
}
