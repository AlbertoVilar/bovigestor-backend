package com.bovigestor.adapter.out.persistence.mapper;

import com.bovigestor.adapter.out.persistence.entity.FarmJpaEntity;
import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;

import java.time.ZoneId;
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
                farm.timeZone().getId(),
                farm.status(),
                farm.createdAt(),
                farm.updatedAt()
        );
    }

    public static Farm toDomain(FarmJpaEntity entity) {
        Objects.requireNonNull(entity, "A entidade JPA da fazenda é obrigatória");
        Objects.requireNonNull(
                entity.getTimeZone(),
                "O fuso horário persistido da fazenda é obrigatório"
        );

        return Farm.reconstitute(
                new FarmId(entity.getId()),
                entity.getName(),
                entity.getCodeSeries(),
                ZoneId.of(entity.getTimeZone()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
