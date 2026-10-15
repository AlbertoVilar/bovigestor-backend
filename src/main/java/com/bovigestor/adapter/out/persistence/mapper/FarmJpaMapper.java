package com.bovigestor.adapter.out.persistence.mapper;

import com.bovigestor.adapter.out.persistence.entity.FarmJpaEntity;
import com.bovigestor.domain.model.Farm;

import java.util.Objects;

public final class FarmJpaMapper {

    private FarmJpaMapper() {
    }

    public static FarmJpaEntity toEntity(Farm farm) {
        Objects.requireNonNull(farm, "A fazenda é obrigatória");

        return new FarmJpaEntity(
                farm.id().value(),
                farm.name(),
                farm.codeSeries(),
                farm.status(),
                farm.createdAt(),
                farm.updatedAt()
        );
    }
}
