package com.bovigestor.adapter.in.web.dto;

import java.time.Instant;

public record FarmResponse(
        Long id,
        String name,
        String codeSeries,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
