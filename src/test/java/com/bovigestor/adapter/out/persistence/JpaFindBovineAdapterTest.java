package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.entity.BovineJpaEntity;
import com.bovigestor.adapter.out.persistence.entity.BovineOriginEmbeddable;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
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
class JpaFindBovineAdapterTest {

    @Mock
    private EntityManager entityManager;

    private JpaFindBovineAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaFindBovineAdapter(entityManager);
    }

    @Test
    void shouldReturnMappedBovineWhenEntityExists() {
        BovineId bovineId = new BovineId(42L);
        BovineJpaEntity entity = bovineEntity();
        when(entityManager.find(BovineJpaEntity.class, bovineId.value()))
                .thenReturn(entity);

        Optional<Bovine> result = adapter.findById(bovineId);

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(bovine -> {
                    assertThat(bovine.id()).isEqualTo(bovineId);
                    assertThat(bovine.internalCode().value()).isEqualTo("BVL-26-0042");
                    assertThat(bovine.name()).isEqualTo("Mimosa");
                    assertThat(bovine.origin().creatorName()).isEqualTo("Criador de teste");
                });
        verify(entityManager).find(BovineJpaEntity.class, bovineId.value());
    }

    @Test
    void shouldReturnEmptyWhenEntityDoesNotExist() {
        BovineId bovineId = new BovineId(99L);
        when(entityManager.find(BovineJpaEntity.class, bovineId.value()))
                .thenReturn(null);

        Optional<Bovine> result = adapter.findById(bovineId);

        assertThat(result).isEmpty();
        verify(entityManager).find(BovineJpaEntity.class, bovineId.value());
    }

    private static BovineJpaEntity bovineEntity() {
        Instant timestamp = Instant.parse("2026-06-01T12:00:00Z");
        return new BovineJpaEntity(
                42L,
                "BVL-26-0042",
                "Mimosa",
                BovineSex.FEMALE,
                "Nelore",
                "Branca",
                null,
                BovineStatus.ACTIVE,
                null,
                null,
                null,
                BovineRegistrationCategory.UNREGISTERED,
                null,
                null,
                null,
                null,
                timestamp,
                timestamp,
                new BovineOriginEmbeddable("Criador de teste", "Fazenda de teste", null)
        );
    }
}
