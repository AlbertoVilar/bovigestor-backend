package com.bovigestor.application.port.in;

import com.bovigestor.domain.model.FarmId;

import java.util.Objects;

public record CreateBovineWithInitialOwnershipCommand(
        CreateBovineCommand bovineData,
        FarmId farmId
) {
    public CreateBovineWithInitialOwnershipCommand {
        Objects.requireNonNull(
                bovineData,
                "Os dados do bovino são obrigatórios"
        );

        Objects.requireNonNull(
                farmId,
                "A fazenda de ingresso é obrigatória"
        );
    }
}
