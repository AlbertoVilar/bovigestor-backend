package com.bovigestor.adapter.in.web.dto;

import java.time.Instant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação completa de uma fazenda, reservada para operações de consulta.")
public record FarmResponse(
        @Schema(description = "Identificador da fazenda.", example = "123")
        Long id,
        @Schema(description = "Nome da fazenda.", example = "Fazenda Boa Vista")
        String name,
        @Schema(description = "Série exclusiva da fazenda.", example = "BVL")
        String codeSeries,
        @Schema(description = "Situação atual da fazenda.", example = "ACTIVE")
        String status,
        @Schema(description = "Instante UTC de criação.", example = "2026-10-15T12:00:00Z")
        Instant createdAt,
        @Schema(description = "Instante UTC da última atualização.", example = "2026-10-15T12:00:00Z")
        Instant updatedAt
) {
}
