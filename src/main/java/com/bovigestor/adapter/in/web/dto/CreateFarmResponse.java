package com.bovigestor.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Identificador da fazenda recém-criada.")
public record CreateFarmResponse(
        @Schema(description = "Identificador positivo da fazenda.", example = "123")
        Long id
) {
}
