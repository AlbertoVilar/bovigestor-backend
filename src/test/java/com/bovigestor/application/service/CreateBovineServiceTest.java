package com.bovigestor.application.service;

import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.application.port.out.BovineIdGenerationPort;
import com.bovigestor.application.port.out.SaveBovinePort;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CreateBovineServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-06-01T12:00:00Z"),
            ZoneOffset.UTC
    );

    @Test
    void shouldCreateAndSaveBovine() {
        // Arrange
        BovineId expectedId = new BovineId(1L);
        BovineIdGenerationPort idGenerationPort = mock(BovineIdGenerationPort.class);
        SaveBovinePort saveBovinePort = mock(SaveBovinePort.class);
        when(idGenerationPort.nextId()).thenReturn(expectedId);
        CreateBovineService service = new CreateBovineService(idGenerationPort, saveBovinePort, CLOCK);
        CreateBovineCommand command = commandWith(
                "BVL-26-0001",
                BovineRegistrationCategory.UNREGISTERED
        );

        // Act
        BovineId createdId = service.create(command);

        // Assert
        assertThat(createdId).isEqualTo(expectedId);
        verify(idGenerationPort).nextId();
        verify(saveBovinePort).save(any(Bovine.class));
    }

    @Test
    void shouldRejectNullCommandWithoutCallingPorts() {
        BovineIdGenerationPort idGenerationPort = mock(BovineIdGenerationPort.class);
        SaveBovinePort saveBovinePort = mock(SaveBovinePort.class);
        CreateBovineService service = new CreateBovineService(idGenerationPort, saveBovinePort, CLOCK);

        assertThatThrownBy(() -> service.create(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O comando de cadastro é obrigatório");
        verifyNoInteractions(idGenerationPort, saveBovinePort);
    }

    @Test
    void shouldNotSaveBovineWhenInternalCodeIsInvalid() {
        BovineIdGenerationPort idGenerationPort = mock(BovineIdGenerationPort.class);
        SaveBovinePort saveBovinePort = mock(SaveBovinePort.class);
        when(idGenerationPort.nextId()).thenReturn(new BovineId(1L));
        CreateBovineService service = new CreateBovineService(idGenerationPort, saveBovinePort, CLOCK);
        CreateBovineCommand command = commandWith(
                "INVALID",
                BovineRegistrationCategory.UNREGISTERED
        );

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O código interno deve seguir o formato");
        verifyNoInteractions(saveBovinePort);
    }

    @Test
    void shouldNotSaveBovineWhenRegistrationCategoryIsMissing() {
        BovineIdGenerationPort idGenerationPort = mock(BovineIdGenerationPort.class);
        SaveBovinePort saveBovinePort = mock(SaveBovinePort.class);
        when(idGenerationPort.nextId()).thenReturn(new BovineId(1L));
        CreateBovineService service = new CreateBovineService(idGenerationPort, saveBovinePort, CLOCK);
        CreateBovineCommand command = commandWith("BVL-26-0001", null);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("A categoria de registro do bovino é obrigatória");
        verifyNoInteractions(saveBovinePort);
    }

    @Test
    void shouldPropagateIdGenerationFailureWithoutSaving() {
        BovineIdGenerationPort idGenerationPort = mock(BovineIdGenerationPort.class);
        SaveBovinePort saveBovinePort = mock(SaveBovinePort.class);
        IllegalStateException failure = new IllegalStateException("Falha simulada na geração do ID");
        when(idGenerationPort.nextId()).thenThrow(failure);
        CreateBovineService service = new CreateBovineService(idGenerationPort, saveBovinePort, CLOCK);
        CreateBovineCommand command = commandWith(
                "BVL-26-0001",
                BovineRegistrationCategory.UNREGISTERED
        );

        assertThatThrownBy(() -> service.create(command))
                .isSameAs(failure);
        verifyNoInteractions(saveBovinePort);
    }

    @Test
    void shouldNotSaveBovineWhenGeneratedIdIsNull() {
        BovineIdGenerationPort idGenerationPort = mock(BovineIdGenerationPort.class);
        SaveBovinePort saveBovinePort = mock(SaveBovinePort.class);
        when(idGenerationPort.nextId()).thenReturn(null);
        CreateBovineService service = new CreateBovineService(idGenerationPort, saveBovinePort, CLOCK);
        CreateBovineCommand command = commandWith(
                "BVL-26-0001",
                BovineRegistrationCategory.UNREGISTERED
        );

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O identificador do bovino é obrigatório");
        verifyNoInteractions(saveBovinePort);
    }

    @Test
    void shouldPropagatePersistenceFailure() {
        BovineIdGenerationPort idGenerationPort = mock(BovineIdGenerationPort.class);
        SaveBovinePort saveBovinePort = mock(SaveBovinePort.class);
        when(idGenerationPort.nextId()).thenReturn(new BovineId(1L));
        IllegalStateException failure = new IllegalStateException("Falha simulada na persistência");
        doThrow(failure).when(saveBovinePort).save(any(Bovine.class));
        CreateBovineService service = new CreateBovineService(idGenerationPort, saveBovinePort, CLOCK);
        CreateBovineCommand command = commandWith(
                "BVL-26-0001",
                BovineRegistrationCategory.UNREGISTERED
        );

        assertThatThrownBy(() -> service.create(command))
                .isSameAs(failure);
        verify(saveBovinePort).save(any(Bovine.class));
    }

    @Test
    void shouldRejectNullIdGenerationPort() {
        SaveBovinePort saveBovinePort = mock(SaveBovinePort.class);

        assertThatThrownBy(() -> new CreateBovineService(null, saveBovinePort, CLOCK))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O gerador de identificadores é obrigatório");
    }

    @Test
    void shouldRejectNullSaveBovinePort() {
        BovineIdGenerationPort idGenerationPort = mock(BovineIdGenerationPort.class);

        assertThatThrownBy(() -> new CreateBovineService(idGenerationPort, null, CLOCK))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("A porta de persistência é obrigatória");
    }

    @Test
    void shouldRejectNullClock() {
        BovineIdGenerationPort idGenerationPort = mock(BovineIdGenerationPort.class);
        SaveBovinePort saveBovinePort = mock(SaveBovinePort.class);

        assertThatThrownBy(() -> new CreateBovineService(idGenerationPort, saveBovinePort, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O relógio da aplicação é obrigatório");
    }

    private static CreateBovineCommand commandWith(
            String internalCode,
            BovineRegistrationCategory category
    ) {
        return new CreateBovineCommand(
                "Mimosa",
                internalCode,
                BovineSex.FEMALE,
                "Nelore",
                "Branca",
                LocalDate.of(2024, 1, 15),
                BovineStatus.ACTIVE,
                "TAG-001",
                null,
                null,
                category,
                null,
                null,
                null,
                null
        );
    }
}
