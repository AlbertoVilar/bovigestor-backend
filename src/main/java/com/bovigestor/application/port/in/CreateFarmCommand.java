package com.bovigestor.application.port.in;

import java.time.ZoneId;
import java.util.Objects;

public record CreateFarmCommand(
        String name,
        String codeSeries,
        ZoneId timeZone
) {
    public CreateFarmCommand {
        Objects.requireNonNull(name, "O nome da fazenda é obrigatório");
        Objects.requireNonNull(codeSeries, "A série da fazenda é obrigatória");
        Objects.requireNonNull(timeZone, "O fuso horário da fazenda é obrigatório");

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
