package com.bovigestor.adapter.in.web.mapper;

import com.bovigestor.adapter.in.web.dto.BovineRequest;
import com.bovigestor.adapter.in.web.dto.BovineResponse;
import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.application.port.in.CreateBovineWithInitialOwnershipCommand;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineOrigin;
import com.bovigestor.domain.model.FarmId;

import java.util.Objects;

public final class BovineWebMapper {

    private BovineWebMapper() {
    }

    public static CreateBovineCommand toCommand(BovineRequest request) {
        Objects.requireNonNull(request, "A solicitação de criação do bovino é obrigatória");

        return new CreateBovineCommand(
                request.name(),
                request.internalCode(),
                request.sex(),
                request.breed(),
                request.coatColor(),
                request.birthDate(),
                request.status(),
                request.earTag(),
                request.registrationNumber(),
                request.registrationAssociation(),
                request.registrationCategory(),
                toBovineId(request.fatherId()),
                toBovineId(request.motherId()),
                request.fatherRegistrationNumber(),
                request.motherRegistrationNumber(),
                toBovineOrigin(request.origin())
        );
    }

    public static CreateBovineWithInitialOwnershipCommand toOperationalCommand(BovineRequest request) {
        Objects.requireNonNull(request, "A solicitação de criação do bovino é obrigatória");
        return new CreateBovineWithInitialOwnershipCommand(
                toCommand(request), new FarmId(request.farmId())
        );
    }

    public static BovineResponse toResponse(Bovine bovine) {
        Objects.requireNonNull(bovine, "O bovino é obrigatório");

        return new BovineResponse(
                bovine.id().value(),
                bovine.internalCode().value(),
                bovine.name(),
                bovine.sex().name(),
                bovine.breed(),
                bovine.coatColor(),
                bovine.birthDate(),
                bovine.status().name(),
                bovine.earTag(),
                bovine.registrationNumber(),
                bovine.registrationAssociation(),
                bovine.registrationCategory().name(),
                bovine.fatherId() == null ? null : bovine.fatherId().value(),
                bovine.motherId() == null ? null : bovine.motherId().value(),
                bovine.fatherRegistrationNumber(),
                bovine.motherRegistrationNumber(),
                bovine.createdAt(),
                bovine.updatedAt(),
                new BovineResponse.OriginResponse(
                        bovine.origin().creatorName(),
                        bovine.origin().originFarmName(),
                        bovine.origin().originFarmId() == null
                                ? null
                                : bovine.origin().originFarmId().value()
                )
        );
    }

    private static BovineOrigin toBovineOrigin(BovineRequest.OriginRequest origin) {
        Objects.requireNonNull(origin, "A origem do bovino é obrigatória");

        return new BovineOrigin(
                origin.creatorName(),
                origin.originFarmName(),
                toFarmId(origin.originFarmId())
        );
    }

    private static BovineId toBovineId(Long id) {
        return id == null ? null : new BovineId(id);
    }

    private static FarmId toFarmId(Long id) {
        return id == null ? null : new FarmId(id);
    }
}
