package com.bovigestor.config;

import com.bovigestor.application.port.in.CreateFarmUseCase;
import com.bovigestor.application.port.out.FarmIdGenerationPort;
import com.bovigestor.application.port.out.SaveFarmPort;
import com.bovigestor.application.service.CreateFarmService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

@Configuration
public class FarmConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public TransactionTemplate transactionTemplate(
            PlatformTransactionManager transactionManager
    ) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public CreateFarmUseCase createFarmUseCase(
            FarmIdGenerationPort farmIdGenerationPort,
            SaveFarmPort saveFarmPort,
            Clock clock,
            TransactionTemplate transactionTemplate
    ) {

        CreateFarmService service = new CreateFarmService(
                farmIdGenerationPort,
                saveFarmPort,
                clock
        );

        return command -> transactionTemplate.execute(
                status -> service.create(command)
        );
    }
}
