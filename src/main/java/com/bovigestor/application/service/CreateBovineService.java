package com.bovigestor.application.service;

import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.application.port.in.CreateBovineUseCase;
import com.bovigestor.application.port.out.BovineIdGenerationPort;
import com.bovigestor.application.port.out.SaveBovinePort;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineInternalCode;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineRegistrationData;

import java.time.Clock;
import java.util.Objects;

public class CreateBovineService implements CreateBovineUseCase {

    private final BovineIdGenerationPort bovineIdGenerationPort;
    private final SaveBovinePort saveBovinePort;
    private final Clock clock;

    public CreateBovineService(
            BovineIdGenerationPort bovineIdGenerationPort,
            SaveBovinePort saveBovinePort,
            Clock clock
    ) {
        this.bovineIdGenerationPort = Objects.requireNonNull(
                bovineIdGenerationPort,
                "O gerador de identificadores é obrigatório"
        );
        this.saveBovinePort = Objects.requireNonNull(
                saveBovinePort,
                "A porta de persistência é obrigatória"
        );
        this.clock = Objects.requireNonNull(clock, "O relógio da aplicação é obrigatório");
    }

    @Override
    public BovineId create(CreateBovineCommand command) {

        Objects.requireNonNull(command, "O comando de cadastro é obrigatório");

        BovineId bovineId = bovineIdGenerationPort.nextId();
        BovineInternalCode internalCode = new BovineInternalCode(command.internalCode());
        Bovine newBovine = Bovine.createManually(
                bovineId,
                internalCode,
                toRegistrationData(command),
                clock.instant()
        );
        saveBovinePort.save(newBovine);
        return bovineId;
    }

    private static BovineRegistrationData toRegistrationData(
            CreateBovineCommand command
    ) {
        return new BovineRegistrationData(
                command.name(),
                command.sex(),
                command.breed(),
                command.coatColor(),
                command.birthDate(),
                command.status(),
                command.earTag(),
                command.registrationNumber(),
                command.registrationAssociation(),
                command.registrationCategory(),
                command.fatherId(),
                command.motherId(),
                command.fatherRegistrationNumber(),
                command.motherRegistrationNumber()
        );
    }
}
