package com.bovigestor.application.service;

import com.bovigestor.application.exceptions.ResourceNotFoundException;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindBovineServiceTest {

    @Mock
    private FindBovinePort findBovinePort;

    @InjectMocks
    private FindBovineService findBovineService;

    @Test
    void shouldReturnBovineWhenFound() {
        // Arrange
        BovineId id = new BovineId(1L);
        Bovine expectedBovine = Bovine.createManually(
                id,
                new BovineInternalCode("BVL-26-0001"),
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
                Instant.parse("2026-06-01T12:00:00Z"),
                new BovineOrigin("Criador de teste", "Fazenda de teste", null)
        );
        when(findBovinePort.findById(id)).thenReturn(Optional.of(expectedBovine));

        // Act
        Bovine result = findBovineService.findById(id);

        // Assert
        assertThat(result).isSameAs(expectedBovine);
        verify(findBovinePort).findById(id);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenBovineDoesNotExist() {
        BovineId id = new BovineId(1L);
        when(findBovinePort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> findBovineService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Bovino não encontrado para o identificador: 1");
        verify(findBovinePort).findById(id);
    }

    @Test
    void shouldRejectNullIdWithoutCallingPort() {
        assertThatThrownBy(() -> findBovineService.findById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O identificador é obrigatório");
        verifyNoInteractions(findBovinePort);
    }
}
