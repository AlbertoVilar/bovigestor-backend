package com.bovigestor.adapter.in.web.mapper;

import com.bovigestor.adapter.in.web.dto.BovineRequest;
import com.bovigestor.application.port.in.CreateBovineCommand;
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
