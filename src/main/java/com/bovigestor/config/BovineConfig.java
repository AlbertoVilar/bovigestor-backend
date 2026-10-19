package com.bovigestor.config;

import com.bovigestor.application.port.in.CreateBovineUseCase;
import com.bovigestor.application.port.out.BovineIdGenerationPort;
import com.bovigestor.application.port.out.SaveBovinePort;
import com.bovigestor.application.service.CreateBovineService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

@Configuration
public class BovineConfig {

    @Bean
    public CreateBovineUseCase createBovineUseCase(
            BovineIdGenerationPort bovineIdGenerationPort,
            SaveBovinePort saveBovinePort,
            Clock clock,
            TransactionTemplate transactionTemplate
    ) {
        CreateBovineService service = new CreateBovineService(
                bovineIdGenerationPort,
                saveBovinePort,
                clock
        );

        return command -> transactionTemplate.execute(
                status -> service.create(command)
        );
    }
}
