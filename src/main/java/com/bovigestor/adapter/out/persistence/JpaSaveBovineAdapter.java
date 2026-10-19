package com.bovigestor.adapter.out.persistence;

import com.bovigestor.adapter.out.persistence.mapper.BovineJpaMapper;
import com.bovigestor.application.port.out.SaveBovinePort;
import com.bovigestor.domain.model.Bovine;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class JpaSaveBovineAdapter implements SaveBovinePort {

    private final EntityManager entityManager;

    public JpaSaveBovineAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(Bovine bovine) {
        var entity = BovineJpaMapper.toEntity(bovine);
        entityManager.persist(entity);
    }
}
