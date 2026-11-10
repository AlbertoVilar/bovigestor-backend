package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.entity.FarmJpaEntity;
import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;
import com.bovigestor.domain.model.FarmStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@SpringBootTest
@ActiveProfiles("test")
class JpaFindFarmAdapterIntegrationTest {

    @Autowired
    private JpaFindFarmAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void shouldLoadAndReconstitutePersistedFarm() {
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T10:00:00Z");
        FarmJpaEntity entity = new FarmJpaEntity(
                2001L,
                "Fazenda Santa Rita",
                "SRT",
                "America/Manaus",
                FarmStatus.INACTIVE,
                createdAt,
                updatedAt
        );
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        Optional<Farm> result = adapter.findById(new FarmId(2001L));

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(farm -> {
                    assertThat(farm.id()).isEqualTo(new FarmId(2001L));
                    assertThat(farm.name()).isEqualTo("Fazenda Santa Rita");
                    assertThat(farm.codeSeries()).isEqualTo("SRT");
                    assertThat(farm.timeZone()).isEqualTo(ZoneId.of("America/Manaus"));
                    assertThat(farm.status()).isEqualTo(FarmStatus.INACTIVE);
                    assertThat(farm.createdAt()).isEqualTo(createdAt);
                    assertThat(farm.updatedAt()).isEqualTo(updatedAt);
                });
    }

    @Test
    @Transactional
    void shouldReturnEmptyForUnknownFarmId() {
        assertThat(adapter.findById(new FarmId(2999L))).isEmpty();
    }

    @Test
    @Transactional
    void shouldFailExplicitlyWhenPersistedLegacyFarmHasNoTimeZone() {
        entityManager.persist(new FarmJpaEntity(
                2002L,
                "Fazenda Legada",
                "LEG",
                null,
                FarmStatus.ACTIVE,
                Instant.parse("2020-01-01T10:00:00Z"),
                Instant.parse("2020-01-01T10:00:00Z")
        ));
        entityManager.flush();
        entityManager.clear();

        assertThatNullPointerException()
                .isThrownBy(() -> adapter.findById(new FarmId(2002L)))
                .withMessage("O fuso horário persistido da fazenda é obrigatório");
    }
}
