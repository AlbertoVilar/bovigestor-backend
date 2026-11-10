package com.bovigestor.adapter.in.web.mapper;

import com.bovigestor.adapter.in.web.dto.FarmResponse;
import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;
import com.bovigestor.domain.model.FarmStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class FarmWebMapperTest {

    @Test
    void shouldMapFarmToResponsePreservingAllReadAttributes() {
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T10:00:00Z");
        Farm farm = Farm.reconstitute(
                new FarmId(17L),
                "Fazenda Santa Rita",
                "SRT",
                ZoneId.of("America/Manaus"),
                FarmStatus.INACTIVE,
                createdAt,
                updatedAt
        );

        FarmResponse response = FarmWebMapper.toResponse(farm);

        assertThat(response.id()).isEqualTo(17L);
        assertThat(response.name()).isEqualTo("Fazenda Santa Rita");
        assertThat(response.codeSeries()).isEqualTo("SRT");
        assertThat(response.timeZone()).isEqualTo("America/Manaus");
        assertThat(response.status()).isEqualTo("INACTIVE");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void shouldRejectNullFarmWhenMappingToResponse() {
        assertThatNullPointerException()
                .isThrownBy(() -> FarmWebMapper.toResponse(null))
                .withMessage("A fazenda é obrigatória");
    }
}
