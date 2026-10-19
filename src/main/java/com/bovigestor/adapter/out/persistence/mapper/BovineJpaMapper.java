package com.bovigestor.adapter.out.persistence.mapper;

import com.bovigestor.adapter.out.persistence.entity.BovineJpaEntity;
import com.bovigestor.domain.model.Bovine;

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
                bovine.updatedAt()
        );
    }
}
