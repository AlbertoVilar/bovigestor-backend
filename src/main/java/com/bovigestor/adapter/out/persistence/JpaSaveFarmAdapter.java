package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.entity.FarmJpaEntity;
import com.bovigestor.adapter.out.persistence.mapper.FarmJpaMapper;
import com.bovigestor.application.port.out.SaveFarmPort;
import com.bovigestor.domain.model.Farm;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class JpaSaveFarmAdapter implements SaveFarmPort {

    private final EntityManager entityManager;

    public JpaSaveFarmAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(Farm farm) {
        FarmJpaEntity entity = FarmJpaMapper.toEntity(farm);
        entityManager.persist(entity);
    }
}
