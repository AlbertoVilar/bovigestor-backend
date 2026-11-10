package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.entity.FarmJpaEntity;
import com.bovigestor.adapter.out.persistence.mapper.FarmJpaMapper;
import com.bovigestor.application.port.out.FindFarmPort;
import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaFindFarmAdapter implements FindFarmPort {

    private final EntityManager entityManager;

    public JpaFindFarmAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Farm> findById(FarmId farmId) {
        FarmJpaEntity entity = entityManager.find(
                FarmJpaEntity.class,
                farmId.value()
        );

        return Optional.ofNullable(entity)
                .map(FarmJpaMapper::toDomain);
    }
}
