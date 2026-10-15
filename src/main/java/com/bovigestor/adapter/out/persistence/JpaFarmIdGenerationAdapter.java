package com.bovigestor.adapter.out.persistence;

import com.bovigestor.application.port.out.FarmIdGenerationPort;
import com.bovigestor.domain.model.FarmId;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class JpaFarmIdGenerationAdapter implements FarmIdGenerationPort {

    private final EntityManager entityManager;

    public JpaFarmIdGenerationAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public FarmId nextId() {

        Number sequenceValue = (Number) entityManager
                .createNativeQuery("SELECT nextval('farm_id_seq')")
                .getSingleResult();

        return new FarmId(sequenceValue.longValue());
    }
}
