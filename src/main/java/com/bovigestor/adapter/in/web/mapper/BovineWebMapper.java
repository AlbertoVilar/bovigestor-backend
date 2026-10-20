package com.bovigestor.adapter.in.web.mapper;

import com.bovigestor.adapter.in.web.dto.BovineRequest;
import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.domain.model.BovineId;

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
                request.motherRegistrationNumber()
        );
    }

    private static BovineId toBovineId(Long id) {
        return id == null ? null : new BovineId(id);
    }
}
