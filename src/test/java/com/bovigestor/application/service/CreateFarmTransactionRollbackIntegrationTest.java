package com.bovigestor.application.service;

import com.bovigestor.adapter.out.persistence.entity.FarmJpaEntity;
import com.bovigestor.application.port.in.CreateFarmCommand;
import com.bovigestor.application.port.out.FarmIdGenerationPort;
import com.bovigestor.application.port.out.SaveFarmPort;
import com.bovigestor.domain.model.FarmId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class CreateFarmTransactionRollbackIntegrationTest {

    private static final String ROLLBACK_MESSAGE = "Intentional failure to verify rollback";

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private FarmIdGenerationPort farmIdGenerationPort;

    @Autowired
    private SaveFarmPort saveFarmPort;

    @Autowired
    private Clock clock;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldRollbackFarmPersistenceWhenFailureOccursBeforeCommit() {
        CreateFarmService service = new CreateFarmService(
                farmIdGenerationPort,
                saveFarmPort,
                clock
        );
        CreateFarmCommand command = new CreateFarmCommand(
                "Fazenda Rollback",
                createUnusedCodeSeries()
        );
        AtomicReference<FarmId> createdId = new AtomicReference<>();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> transactionTemplate.executeWithoutResult(status -> {
                    FarmId farmId = service.create(command);
                    createdId.set(farmId);

                    entityManager.flush();
                    entityManager.clear();

                    FarmJpaEntity persistedFarm = entityManager.find(
                            FarmJpaEntity.class,
                            farmId.value()
                    );
                    assertNotNull(persistedFarm);

                    throw new IllegalStateException(ROLLBACK_MESSAGE);
                })
        );

        assertEquals(ROLLBACK_MESSAGE, exception.getMessage());

        FarmId farmId = createdId.get();
        assertNotNull(farmId);
        assertNull(entityManager.find(FarmJpaEntity.class, farmId.value()));
    }

    private String createUnusedCodeSeries() {
        for (int attempt = 0; attempt < 100; attempt++) {
            String candidate = createRandomCodeSeries();
            Long existingCount = entityManager.createQuery(
                            "select count(f) from FarmJpaEntity f where f.codeSeries = :codeSeries",
                            Long.class
                    )
                    .setParameter("codeSeries", candidate)
                    .getSingleResult();

            if (existingCount == 0) {
                return candidate;
            }
        }

        throw new IllegalStateException("Could not generate an unused farm code series");
    }

    private String createRandomCodeSeries() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder codeSeries = new StringBuilder(4);

        for (int index = 0; index < 4; index++) {
            codeSeries.append((char) ('A' + random.nextInt(26)));
        }

        return codeSeries.toString();
    }
}
