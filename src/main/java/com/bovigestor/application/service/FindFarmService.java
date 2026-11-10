package com.bovigestor.application.service;

import com.bovigestor.application.exceptions.ResourceNotFoundException;
import com.bovigestor.application.port.in.FindFarmByIdUseCase;
import com.bovigestor.application.port.out.FindFarmPort;
import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;

import java.util.Objects;

public class FindFarmService implements FindFarmByIdUseCase {

    private final FindFarmPort findFarmPort;

    public FindFarmService(FindFarmPort findFarmPort) {
        this.findFarmPort = Objects.requireNonNull(
                findFarmPort, "A porta de consulta de Farm é obrigatória"
        );
    }

    @Override
    public Farm findById(FarmId farmId) {
        Objects.requireNonNull(farmId, "O identificador é obrigatório");
        return findFarmPort.findById(farmId).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Fazenda não encontrada para o identificador: "
                                + farmId.value()
                )
        );
    }
}
