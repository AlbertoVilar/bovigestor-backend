package com.bovigestor.config;

import com.bovigestor.application.port.in.FindBovineByIdUseCase;
import com.bovigestor.application.port.in.CreateBovineWithInitialOwnershipUseCase;
import com.bovigestor.application.port.out.BovineIdGenerationPort;
import com.bovigestor.application.port.out.BovineOwnershipPeriodIdGenerationPort;
import com.bovigestor.application.port.out.FindFarmPort;
import com.bovigestor.application.port.out.FindBovinePort;
import com.bovigestor.application.port.out.SaveBovinePort;
import com.bovigestor.application.port.out.SaveBovineOwnershipPeriodPort;
import com.bovigestor.application.service.CreateBovineWithInitialOwnershipService;
import com.bovigestor.application.service.FindBovineService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

@Configuration
public class BovineConfig {

    @Bean
    public CreateBovineWithInitialOwnershipUseCase createBovineWithInitialOwnershipUseCase(
            BovineIdGenerationPort bovineIdGenerationPort,
            SaveBovinePort saveBovinePort,
            FindFarmPort findFarmPort,
            BovineOwnershipPeriodIdGenerationPort periodIdGenerationPort,
            SaveBovineOwnershipPeriodPort savePeriodPort,
            Clock clock,
            TransactionTemplate transactionTemplate
    ) {
        var service = new CreateBovineWithInitialOwnershipService(
                bovineIdGenerationPort, saveBovinePort, findFarmPort,
                periodIdGenerationPort, savePeriodPort, clock
        );
        return command -> transactionTemplate.execute(status -> service.create(command));
    }

    @Bean
    public FindBovineByIdUseCase findBovineByIdUseCase(
            FindBovinePort findBovinePort
    ) {
        return new FindBovineService(findBovinePort);
    }
}
