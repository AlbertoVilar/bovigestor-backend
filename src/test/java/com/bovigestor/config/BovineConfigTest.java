package com.bovigestor.config;

import com.bovigestor.application.port.in.FindBovineByIdUseCase;
import com.bovigestor.application.port.out.FindBovinePort;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineInternalCode;
import com.bovigestor.domain.model.BovineOrigin;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineRegistrationData;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BovineConfigTest {

    @Test
    void shouldProvideFindBovineUseCaseDelegatingToPort() {
        FindBovinePort findBovinePort = mock(FindBovinePort.class);
        BovineId bovineId = new BovineId(42L);
        Bovine expected = bovine();
        when(findBovinePort.findById(bovineId)).thenReturn(Optional.of(expected));

        FindBovineByIdUseCase useCase = new BovineConfig()
                .findBovineByIdUseCase(findBovinePort);

        assertThat(useCase.findById(bovineId)).isSameAs(expected);
        verify(findBovinePort).findById(bovineId);
    }

    private static Bovine bovine() {
        Instant createdAt = Instant.parse("2026-06-01T12:00:00Z");
        return Bovine.createManually(
                new BovineId(42L),
                new BovineInternalCode("BVL-26-0042"),
                new BovineRegistrationData(
                        "Mimosa",
                        BovineSex.FEMALE,
                        null,
                        null,
                        null,
                        BovineStatus.ACTIVE,
                        null,
                        null,
                        null,
                        BovineRegistrationCategory.UNREGISTERED,
                        null,
                        null,
                        null,
                        null
                ),
                createdAt,
                new BovineOrigin("Criador de teste", "Fazenda de teste", null)
        );
    }
}
