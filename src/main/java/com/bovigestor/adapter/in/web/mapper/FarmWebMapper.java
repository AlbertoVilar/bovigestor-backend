package com.bovigestor.adapter.in.web.mapper;

import com.bovigestor.adapter.in.web.dto.FarmRequest;
import com.bovigestor.application.port.in.CreateFarmCommand;

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
                request.codeSeries()
        );
    }
}
