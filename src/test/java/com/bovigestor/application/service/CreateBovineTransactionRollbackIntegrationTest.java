package com.bovigestor.application.service;

import com.bovigestor.adapter.out.persistence.entity.BovineJpaEntity;
import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.application.port.in.CreateBovineUseCase;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class CreateBovineTransactionRollbackIntegrationTest {

    private static final String ROLLBACK_MESSAGE = "Intentional failure to verify rollback";

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private CreateBovineUseCase createBovineUseCase;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldRollbackBovinePersistenceWhenFailureOccursBeforeCommit() {
        CreateBovineCommand command = new CreateBovineCommand(
                "Rollback",
                createInternalCode(),
                BovineSex.FEMALE,
                null,
                null,
                null,
                BovineStatus.ACTIVE,
                null,
                null,
                null,
                BovineRegistrationCategory.UNREGISTERED,
                null,
                null,
                null,
                null
        );
        AtomicReference<BovineId> createdId = new AtomicReference<>();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> transactionTemplate.executeWithoutResult(status -> {
                    BovineId bovineId = createBovineUseCase.create(command);
                    createdId.set(bovineId);

                    entityManager.flush();
                    entityManager.clear();

                    BovineJpaEntity persistedBovine = entityManager.find(
                            BovineJpaEntity.class,
                            bovineId.value()
                    );
                    assertNotNull(persistedBovine);

                    throw new IllegalStateException(ROLLBACK_MESSAGE);
                })
        );

        assertEquals(ROLLBACK_MESSAGE, exception.getMessage());

        BovineId bovineId = createdId.get();
        assertNotNull(bovineId);
        assertNull(entityManager.find(BovineJpaEntity.class, bovineId.value()));
    }

    private String createInternalCode() {
        long sequence = ThreadLocalRandom.current().nextLong(1, 100_000_000L);
        return "TST-26-%08d".formatted(sequence);
    }
}
