package com.bovigestor.adapter.out.persistence;

import com.bovigestor.domain.model.FarmId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JpaFarmIdGenerationAdapterTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldGenerateDifferentFarmIds() {

        var adapter = new JpaFarmIdGenerationAdapter(entityManager);

        FarmId firstId = adapter.nextId();
        FarmId secondId = adapter.nextId();

        assertNotNull(firstId);
        assertNotNull(secondId);

        assertTrue(firstId.value() > 0);
        assertTrue(secondId.value() > firstId.value());
    }
}
