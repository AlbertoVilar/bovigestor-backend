package com.bovigestor.application.service;

import com.bovigestor.application.exceptions.ResourceNotFoundException;
import com.bovigestor.application.port.in.FindBovineByIdUseCase;
import com.bovigestor.application.port.out.FindBovinePort;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;

import java.util.Objects;

public class FindBovineService implements FindBovineByIdUseCase {

    private final FindBovinePort findBovinePort;

    public FindBovineService(FindBovinePort findBovinePort) {
        this.findBovinePort = Objects.requireNonNull(
                findBovinePort,
                "A porta de consulta de bovinos é obrigatória"
        );
    }

    @Override
    public Bovine findById(BovineId bovineId) {
        Objects.requireNonNull(
                bovineId,
                "O identificador é obrigatório"
        );

        return findBovinePort.findById(bovineId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bovino não encontrado para o identificador: "
                                + bovineId.value()
                ));
    }
}
