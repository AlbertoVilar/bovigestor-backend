package com.bovigestor.adapter.out.persistence;

import com.bovigestor.application.port.out.BovineOwnershipPeriodIdGenerationPort;
import com.bovigestor.domain.model.BovineOwnershipPeriodId;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class JpaBovineOwnershipPeriodIdGenerationAdapter implements BovineOwnershipPeriodIdGenerationPort {

    private final EntityManager entityManager;

    public JpaBovineOwnershipPeriodIdGenerationAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public BovineOwnershipPeriodId nextId() {
        Number sequenceValue = (Number) entityManager
                .createNativeQuery("SELECT nextval('bovine_ownership_period_id_seq')")
                .getSingleResult();

        return new BovineOwnershipPeriodId(sequenceValue.longValue());
    }
}
