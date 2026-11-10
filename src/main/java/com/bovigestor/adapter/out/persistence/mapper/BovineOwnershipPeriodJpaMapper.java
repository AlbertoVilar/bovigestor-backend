package com.bovigestor.adapter.out.persistence.mapper;

import com.bovigestor.adapter.out.persistence.entity.BovineOwnershipPeriodJpaEntity;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineOwnershipPeriod;
import com.bovigestor.domain.model.BovineOwnershipPeriodId;
import com.bovigestor.domain.model.FarmId;

import java.util.Objects;

public final class BovineOwnershipPeriodJpaMapper {

    private BovineOwnershipPeriodJpaMapper() {
    }

    public static BovineOwnershipPeriodJpaEntity toEntity(BovineOwnershipPeriod period) {
        Objects.requireNonNull(period, "O período de custódia é obrigatório");

        return new BovineOwnershipPeriodJpaEntity(
                period.id().value(),
                period.bovineId().value(),
                period.farmId().value(),
                period.startedAt(),
                period.endedAt()
        );
    }

    public static BovineOwnershipPeriod toDomain(BovineOwnershipPeriodJpaEntity entity) {
        Objects.requireNonNull(entity, "A entidade do período de custódia é obrigatória");

        return BovineOwnershipPeriod.reconstitute(
                new BovineOwnershipPeriodId(entity.getId()),
                new BovineId(entity.getBovineId()),
                new FarmId(entity.getFarmId()),
                entity.getStartedAt(),
                entity.getEndedAt()
        );
    }
}
