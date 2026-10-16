package com.bovigestor.application.port.in;

import java.util.Objects;

public record CreateFarmCommand(
        String name,
        String codeSeries
) {
    public CreateFarmCommand {
        Objects.requireNonNull(name, "O nome da fazenda é obrigatório");
        Objects.requireNonNull(codeSeries, "A série da fazenda é obrigatória");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da fazenda não pode estar vazio ou conter somente espaços"
            );
        }

        if (codeSeries.isBlank()) {
            throw new IllegalArgumentException(
                    "A série da fazenda não pode estar vazia"
            );
        }
    }
}