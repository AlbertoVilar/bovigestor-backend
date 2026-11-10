package com.bovigestor.adapter.in.web.mapper;

import com.bovigestor.adapter.in.web.dto.FarmRequest;
import com.bovigestor.adapter.in.web.dto.FarmResponse;
import com.bovigestor.application.port.in.CreateFarmCommand;
import com.bovigestor.domain.model.Farm;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Objects;

public final class FarmWebMapper {

    private FarmWebMapper() {
    }

    public static CreateFarmCommand toCommand(FarmRequest request) {
        Objects.requireNonNull(
                request,
                "A solicitação de criação da fazenda é obrigatória"
        );

        return new CreateFarmCommand(
                request.name(),
                request.codeSeries(),
                toZoneId(request.timeZone())
        );
    }

    public static FarmResponse toResponse(Farm farm) {
        Objects.requireNonNull(farm, "A fazenda é obrigatória");

        return new FarmResponse(
                farm.id().value(),
                farm.name(),
                farm.codeSeries(),
                farm.timeZone().getId(),
                farm.status().name(),
                farm.createdAt(),
                farm.updatedAt()
        );
    }

    private static ZoneId toZoneId(String timeZone) {
        try {
            return ZoneId.of(timeZone);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(
                    "O identificador de fuso horário informado é inválido",
                    exception
            );
        }
    }
}
