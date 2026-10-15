package com.bovigestor.application.service;

import com.bovigestor.application.port.in.CreateFarmCommand;
import com.bovigestor.application.port.in.CreateFarmUseCase;
import com.bovigestor.application.port.out.FarmIdGenerationPort;
import com.bovigestor.application.port.out.SaveFarmPort;
import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;

import java.time.Clock;
import java.util.Objects;

public class CreateFarmService implements CreateFarmUseCase {

    private final FarmIdGenerationPort farmIdGenerationPort;
    private final SaveFarmPort saveFarmPort;
    private final Clock clock;

    public CreateFarmService(
            FarmIdGenerationPort farmIdGenerationPort,
            SaveFarmPort saveFarmPort,
            Clock clock
    ) {
        this.farmIdGenerationPort = Objects.requireNonNull(
                farmIdGenerationPort,
                "A porta de geração de FarmId é obrigatória"
        );

        this.saveFarmPort = Objects.requireNonNull(
                saveFarmPort,
                "A porta de persistência de Farm é obrigatória"
        );

        this.clock = Objects.requireNonNull(
                clock,
                "O relógio da aplicação é obrigatório"
        );
    }

    @Override
    public FarmId create(CreateFarmCommand command) {

        Objects.requireNonNull(
                command,
                "O comando de criação da fazenda é obrigatório"
        );

        FarmId id = farmIdGenerationPort.nextId();

        Farm newFarm = Farm.create(
                id,
                command.name(),
                command.codeSeries(),
                clock.instant()
        );

        saveFarmPort.save(newFarm);

        return id;
    }
}
