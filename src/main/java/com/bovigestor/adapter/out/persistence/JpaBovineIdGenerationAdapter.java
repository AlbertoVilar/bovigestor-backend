package com.bovigestor.adapter.out.persistence;

import com.bovigestor.application.port.out.BovineIdGenerationPort;
import com.bovigestor.domain.model.BovineId;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class JpaBovineIdGenerationAdapter implements BovineIdGenerationPort {

    private final EntityManager entityManager;

    public JpaBovineIdGenerationAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public BovineId nextId() {
        Number sequenceValue = (Number) entityManager
                .createNativeQuery("SELECT nextval('bovine_id_seq')")
                .getSingleResult();

        return new BovineId(sequenceValue.longValue());
    }
}
