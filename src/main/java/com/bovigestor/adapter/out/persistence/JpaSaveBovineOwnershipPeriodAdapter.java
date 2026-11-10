package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.mapper.BovineOwnershipPeriodJpaMapper;
import com.bovigestor.application.port.out.SaveBovineOwnershipPeriodPort;
import com.bovigestor.domain.model.BovineOwnershipPeriod;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class JpaSaveBovineOwnershipPeriodAdapter implements SaveBovineOwnershipPeriodPort {
    private final EntityManager entityManager;

    public JpaSaveBovineOwnershipPeriodAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(BovineOwnershipPeriod period) {
        entityManager.persist(BovineOwnershipPeriodJpaMapper.toEntity(period));
    }
}
