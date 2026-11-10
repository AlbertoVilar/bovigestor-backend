package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.entity.BovineJpaEntity;
import com.bovigestor.adapter.out.persistence.mapper.BovineJpaMapper;
import com.bovigestor.application.port.out.FindBovinePort;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaFindBovineAdapter implements FindBovinePort {

    private final EntityManager entityManager;

    public JpaFindBovineAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Bovine> findById(BovineId bovineId) {
        BovineJpaEntity entity = entityManager.find(
                BovineJpaEntity.class,
                bovineId.value()
        );

        return Optional.ofNullable(entity)
                .map(BovineJpaMapper::toDomain);
    }
}
