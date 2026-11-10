package com.bovigestor.application.service;

import com.bovigestor.application.exceptions.InactiveFarmException;
import com.bovigestor.application.exceptions.ResourceNotFoundException;
import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.application.port.in.CreateBovineWithInitialOwnershipCommand;
import com.bovigestor.application.port.in.CreateBovineWithInitialOwnershipUseCase;
import com.bovigestor.application.port.out.BovineIdGenerationPort;
import com.bovigestor.application.port.out.BovineOwnershipPeriodIdGenerationPort;
import com.bovigestor.application.port.out.FindFarmPort;
import com.bovigestor.application.port.out.SaveBovinePort;
import com.bovigestor.application.port.out.SaveBovineOwnershipPeriodPort;
import com.bovigestor.domain.model.*;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class CreateBovineWithInitialOwnershipService
        implements CreateBovineWithInitialOwnershipUseCase {

    private final BovineIdGenerationPort bovineIdGenerationPort;
    private final SaveBovinePort saveBovinePort;
    private final FindFarmPort findFarmPort;
    private final BovineOwnershipPeriodIdGenerationPort periodIdGenerationPort;
    private final SaveBovineOwnershipPeriodPort saveBovineOwnershipPeriodPort;
    private final Clock clock;

    public CreateBovineWithInitialOwnershipService(
            BovineIdGenerationPort bovineIdGenerationPort,
            SaveBovinePort saveBovinePort,
            FindFarmPort findFarmPort,
            BovineOwnershipPeriodIdGenerationPort periodIdGenerationPort,
            SaveBovineOwnershipPeriodPort saveBovineOwnershipPeriodPort,
            Clock clock
    ) {
        this.bovineIdGenerationPort = Objects.requireNonNull(
                bovineIdGenerationPort,
                "O gerador de identificadores do bovino é obrigatório"
        );
        this.saveBovinePort = Objects.requireNonNull(
                saveBovinePort,
                "A porta de persistência do bovino é obrigatória"
        );
        this.findFarmPort = Objects.requireNonNull(
                findFarmPort,
                "A porta de consulta de fazendas é obrigatória"
        );
        this.periodIdGenerationPort = Objects.requireNonNull(
                periodIdGenerationPort,
                "O gerador de identificadores de períodos é obrigatório"
        );
        this.saveBovineOwnershipPeriodPort = Objects.requireNonNull(
                saveBovineOwnershipPeriodPort,
                "A porta de persistência do período é obrigatória"
        );
        this.clock = Objects.requireNonNull(clock, "O relógio da aplicação é obrigatório");
    }

    @Override
    public BovineId create(CreateBovineWithInitialOwnershipCommand command) {

        Objects.requireNonNull(
                command,
                "O comando de cadastro operacional é obrigatório"
        );

        Farm farm = findFarmPort.findById(command.farmId()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Fazenda não encontrada para o identificador: "
                                + command.farmId().value()
                )
        );

        if (farm.status() == FarmStatus.INACTIVE) {
            throw new InactiveFarmException(
                    "A fazenda inativa não pode receber novos bovinos"
            );
        }

        CreateBovineCommand bovineData = command.bovineData();
        BovineId bovineId = bovineIdGenerationPort.nextId();
        Instant registeredAt = clock.instant();

        Bovine newBovine = createBovine(bovineData, bovineId, registeredAt);

        saveBovinePort.save(newBovine);

        BovineOwnershipPeriod period = BovineOwnershipPeriod.open(
                periodIdGenerationPort.nextId(),
                bovineId,
                farm.id(),
                registeredAt
        );
        saveBovineOwnershipPeriodPort.save(period);
        return bovineId;
    }

    private Bovine createBovine(
            CreateBovineCommand bovineData,
            BovineId bovineId,
            Instant registeredAt
    ) {
        return Bovine.createManually(
                bovineId,
                new BovineInternalCode(bovineData.internalCode()),
                new BovineRegistrationData(
                        bovineData.name(),
                        bovineData.sex(),
                        bovineData.breed(),
                        bovineData.coatColor(),
                        bovineData.birthDate(),
                        bovineData.status(),
                        bovineData.earTag(),
                        bovineData.registrationNumber(),
                        bovineData.registrationAssociation(),
                        bovineData.registrationCategory(),
                        bovineData.fatherId(),
                        bovineData.motherId(),
                        bovineData.fatherRegistrationNumber(),
                        bovineData.motherRegistrationNumber()
                ),
                registeredAt,
                bovineData.origin()
        );
    }
}
