package com.bovigestor.adapter.out.persistence.mapper;

import com.bovigestor.adapter.out.persistence.entity.BovineJpaEntity;
import com.bovigestor.adapter.out.persistence.entity.BovineOriginEmbeddable;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineInternalCode;
import com.bovigestor.domain.model.BovineOrigin;
import com.bovigestor.domain.model.BovineRegistrationData;
import com.bovigestor.domain.model.FarmId;

import java.util.Objects;

public final class BovineJpaMapper {

    private BovineJpaMapper() {
    }

    public static BovineJpaEntity toEntity(Bovine bovine) {
        Objects.requireNonNull(bovine, "O bovino é obrigatório");

        return new BovineJpaEntity(
                bovine.id().value(),
                bovine.internalCode().value(),
                bovine.name(),
                bovine.sex(),
                bovine.breed(),
                bovine.coatColor(),
                bovine.birthDate(),
                bovine.status(),
                bovine.earTag(),
                bovine.registrationNumber(),
                bovine.registrationAssociation(),
                bovine.registrationCategory(),
                bovine.fatherId() == null ? null : bovine.fatherId().value(),
                bovine.motherId() == null ? null : bovine.motherId().value(),
                bovine.fatherRegistrationNumber(),
                bovine.motherRegistrationNumber(),
                bovine.createdAt(),
                bovine.updatedAt(),
                new BovineOriginEmbeddable(
                        bovine.origin().creatorName(),
                        bovine.origin().originFarmName(),
                        bovine.origin().originFarmId() == null
                                ? null
                                : bovine.origin().originFarmId().value()
                )
        );
    }

    public static Bovine toDomain(BovineJpaEntity entity) {
        Objects.requireNonNull(entity, "A entidade JPA do bovino é obrigatória");

        BovineOriginEmbeddable origin = Objects.requireNonNull(
                entity.getOrigin(),
                "A origem persistida do bovino é obrigatória"
        );

        return Bovine.reconstitute(
                new BovineId(entity.getId()),
                new BovineInternalCode(entity.getInternalCode()),
                new BovineRegistrationData(
                        entity.getName(),
                        entity.getSex(),
                        entity.getBreed(),
                        entity.getCoatColor(),
                        entity.getBirthDate(),
                        entity.getStatus(),
                        entity.getEarTag(),
                        entity.getRegistrationNumber(),
                        entity.getRegistrationAssociation(),
                        entity.getRegistrationCategory(),
                        toBovineId(entity.getFatherId()),
                        toBovineId(entity.getMotherId()),
                        entity.getFatherRegistrationNumber(),
                        entity.getMotherRegistrationNumber()
                ),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                new BovineOrigin(
                        origin.getCreatorName(),
                        origin.getOriginFarmName(),
                        toFarmId(origin.getOriginFarmId())
                )
        );
    }

    private static BovineId toBovineId(Long id) {
        return id == null ? null : new BovineId(id);
    }

    private static FarmId toFarmId(Long id) {
        return id == null ? null : new FarmId(id);
    }
}
