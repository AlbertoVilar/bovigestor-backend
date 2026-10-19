package com.bovigestor.adapter.out.persistence;

import com.bovigestor.domain.model.BovineId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class JpaBovineIdGenerationAdapterTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldGenerateDifferentPositiveBovineIds() {
        JpaBovineIdGenerationAdapter adapter = new JpaBovineIdGenerationAdapter(entityManager);

        BovineId firstId = adapter.nextId();
        BovineId secondId = adapter.nextId();

        assertNotNull(firstId);
        assertNotNull(secondId);
        assertTrue(firstId.value() > 0);
        assertTrue(secondId.value() > firstId.value());
    }
}
