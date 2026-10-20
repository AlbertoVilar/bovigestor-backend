package com.bovigestor.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Identificador do bovino recém-cadastrado.")
public record CreateBovineResponse(
        @Schema(description = "Identificador positivo do bovino.", example = "123")
        Long id
) {
}
