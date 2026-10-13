package com.bovigestor.application.service;

import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.application.port.in.CreateBovineUseCase;
import com.bovigestor.application.port.out.BovineIdGenerationPort;
import com.bovigestor.application.port.out.SaveBovinePort;
import com.bovigestor.domain.model.BovineInternalCode;
import com.bovigestor.domain.model.BovineId;

import java.util.Objects;

public class CreateBovineService implements CreateBovineUseCase {

    private final BovineIdGenerationPort bovineIdGenerationPort;
    private final SaveBovinePort saveBovinePort;

    public CreateBovineService(BovineIdGenerationPort bovineIdGenerationPort, SaveBovinePort saveBovinePort) {
        this.bovineIdGenerationPort = bovineIdGenerationPort;
        this.saveBovinePort = saveBovinePort;
    }

    @Override
    public BovineId create(CreateBovineCommand command) {

        Objects.requireNonNull(command, "O comando de cadastro é obrigatório");
        BovineId bovineId = bovineIdGenerationPort.nextId();
        BovineInternalCode internalCode =
                new BovineInternalCode(command.internalCode());
        return null;
    }
}
