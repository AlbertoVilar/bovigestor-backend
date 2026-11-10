package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.entity.FarmJpaEntity;
import com.bovigestor.application.port.out.FindFarmPort;
import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;
import com.bovigestor.domain.model.FarmStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaFindFarmAdapterTest {

    @Mock
    private EntityManager entityManager;

    private JpaFindFarmAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaFindFarmAdapter(entityManager);
    }

    @Test
    void shouldReturnMappedFarmWhenEntityExists() {
        FarmId farmId = new FarmId(42L);
        when(entityManager.find(FarmJpaEntity.class, farmId.value()))
                .thenReturn(farmEntity());

        Optional<Farm> result = adapter.findById(farmId);

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(farm -> {
                    assertThat(farm.id()).isEqualTo(farmId);
                    assertThat(farm.name()).isEqualTo("Fazenda Santa Rita");
                    assertThat(farm.timeZone().getId()).isEqualTo("America/Manaus");
                    assertThat(farm.status()).isEqualTo(FarmStatus.INACTIVE);
                    assertThat(farm.updatedAt())
                            .isEqualTo(Instant.parse("2026-01-02T10:00:00Z"));
                });

        verify(entityManager).find(FarmJpaEntity.class, farmId.value());
    }

    @Test
    void shouldReturnEmptyWhenEntityDoesNotExist() {
        FarmId farmId = new FarmId(99L);
        when(entityManager.find(FarmJpaEntity.class, farmId.value()))
                .thenReturn(null);

        Optional<Farm> result = adapter.findById(farmId);

        assertThat(result).isEmpty();
        verify(entityManager).find(FarmJpaEntity.class, farmId.value());
    }

    private static FarmJpaEntity farmEntity() {
        return new FarmJpaEntity(
                42L,
                "Fazenda Santa Rita",
                "SRT",
                "America/Manaus",
                FarmStatus.INACTIVE,
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-02T10:00:00Z")
        );
    }
}
