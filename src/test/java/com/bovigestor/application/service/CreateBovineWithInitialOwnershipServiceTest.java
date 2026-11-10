package com.bovigestor.application.service;

import com.bovigestor.application.exceptions.InactiveFarmException;
import com.bovigestor.application.exceptions.ResourceNotFoundException;
import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.application.port.in.CreateBovineWithInitialOwnershipCommand;
import com.bovigestor.application.port.out.BovineIdGenerationPort;
import com.bovigestor.application.port.out.BovineOwnershipPeriodIdGenerationPort;
import com.bovigestor.application.port.out.FindFarmPort;
import com.bovigestor.application.port.out.SaveBovineOwnershipPeriodPort;
import com.bovigestor.application.port.out.SaveBovinePort;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineOrigin;
import com.bovigestor.domain.model.BovineOwnershipPeriod;
import com.bovigestor.domain.model.BovineOwnershipPeriodId;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;
import com.bovigestor.domain.model.FarmStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CreateBovineWithInitialOwnershipServiceTest {

    private static final Instant REGISTERED_AT = Instant.parse("2026-06-01T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(REGISTERED_AT, ZoneOffset.UTC);
    private static final BovineId BOVINE_ID = new BovineId(101L);
    private static final BovineOwnershipPeriodId PERIOD_ID =
            new BovineOwnershipPeriodId(201L);
    private static final BovineOrigin BOVINE_ORIGIN = new BovineOrigin(
            "Criador de teste",
            "Fazenda de origem",
            null
    );

    private final BovineIdGenerationPort bovineIdGenerationPort =
            mock(BovineIdGenerationPort.class);
    private final SaveBovinePort saveBovinePort = mock(SaveBovinePort.class);
    private final FindFarmPort findFarmPort = mock(FindFarmPort.class);
    private final BovineOwnershipPeriodIdGenerationPort periodIdGenerationPort =
            mock(BovineOwnershipPeriodIdGenerationPort.class);
    private final SaveBovineOwnershipPeriodPort savePeriodPort =
            mock(SaveBovineOwnershipPeriodPort.class);

    private CreateBovineWithInitialOwnershipService service;

    @BeforeEach
    void setUp() {
        service = new CreateBovineWithInitialOwnershipService(
                bovineIdGenerationPort,
                saveBovinePort,
                findFarmPort,
                periodIdGenerationPort,
                savePeriodPort,
                CLOCK
        );
        when(bovineIdGenerationPort.nextId()).thenReturn(BOVINE_ID);
        when(periodIdGenerationPort.nextId()).thenReturn(PERIOD_ID);
    }

    @Test
    void shouldCreateBovineAndInitialPeriodWhenFarmIsActive() {
        Farm farm = farmWithStatus(FarmStatus.ACTIVE, 10L);
        when(findFarmPort.findById(new FarmId(10L))).thenReturn(Optional.of(farm));

        BovineId createdId = service.create(commandFor(new FarmId(10L)));

        assertThat(createdId).isEqualTo(BOVINE_ID);
        verify(saveBovinePort).save(org.mockito.ArgumentMatchers.any(Bovine.class));
        verify(savePeriodPort).save(org.mockito.ArgumentMatchers.any(BovineOwnershipPeriod.class));
    }

    @Test
    void shouldRejectInactiveFarmWithBusinessException() {
        Farm farm = farmWithStatus(FarmStatus.INACTIVE, 10L);
        when(findFarmPort.findById(new FarmId(10L))).thenReturn(Optional.of(farm));

        assertThatThrownBy(() -> service.create(commandFor(new FarmId(10L))))
                .isInstanceOf(InactiveFarmException.class)
                .hasMessage("A fazenda inativa não pode receber novos bovinos");
    }

    @Test
    void shouldNotGenerateIdsWhenFarmIsInactive() {
        Farm farm = farmWithStatus(FarmStatus.INACTIVE, 10L);
        when(findFarmPort.findById(new FarmId(10L))).thenReturn(Optional.of(farm));

        assertThatThrownBy(() -> service.create(commandFor(new FarmId(10L))))
                .isInstanceOf(InactiveFarmException.class);

        verifyNoInteractions(bovineIdGenerationPort, periodIdGenerationPort);
    }

    @Test
    void shouldNotPersistAnythingWhenFarmIsInactive() {
        Farm farm = farmWithStatus(FarmStatus.INACTIVE, 10L);
        when(findFarmPort.findById(new FarmId(10L))).thenReturn(Optional.of(farm));

        assertThatThrownBy(() -> service.create(commandFor(new FarmId(10L))))
                .isInstanceOf(InactiveFarmException.class);

        verifyNoInteractions(saveBovinePort, savePeriodPort);
    }

    @Test
    void shouldKeepNotFoundExceptionWhenFarmDoesNotExist() {
        FarmId requestedFarmId = new FarmId(10L);
        when(findFarmPort.findById(requestedFarmId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(commandFor(requestedFarmId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Fazenda não encontrada para o identificador: 10");

        verifyNoInteractions(
                bovineIdGenerationPort,
                periodIdGenerationPort,
                saveBovinePort,
                savePeriodPort
        );
    }

    @Test
    void shouldRejectNullCommandBeforeCallingPorts() {
        assertThatThrownBy(() -> service.create(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O comando de cadastro operacional é obrigatório");

        verifyNoInteractions(
                findFarmPort,
                bovineIdGenerationPort,
                periodIdGenerationPort,
                saveBovinePort,
                savePeriodPort
        );
    }

    @Test
    void shouldUseSameInstantForBovineAndInitialPeriod() {
        Farm farm = farmWithStatus(FarmStatus.ACTIVE, 10L);
        when(findFarmPort.findById(new FarmId(10L))).thenReturn(Optional.of(farm));

        service.create(commandFor(new FarmId(10L)));

        ArgumentCaptor<Bovine> bovineCaptor = ArgumentCaptor.forClass(Bovine.class);
        ArgumentCaptor<BovineOwnershipPeriod> periodCaptor =
                ArgumentCaptor.forClass(BovineOwnershipPeriod.class);
        verify(saveBovinePort).save(bovineCaptor.capture());
        verify(savePeriodPort).save(periodCaptor.capture());

        assertThat(bovineCaptor.getValue().createdAt()).isEqualTo(REGISTERED_AT);
        assertThat(bovineCaptor.getValue().updatedAt()).isEqualTo(REGISTERED_AT);
        assertThat(periodCaptor.getValue().startedAt()).isEqualTo(REGISTERED_AT);
    }

    @Test
    void shouldUseIdFromFoundFarmForInitialPeriod() {
        FarmId requestedFarmId = new FarmId(10L);
        Farm farm = farmWithStatus(FarmStatus.ACTIVE, 10L);
        when(findFarmPort.findById(requestedFarmId)).thenReturn(Optional.of(farm));

        service.create(commandFor(requestedFarmId));

        ArgumentCaptor<BovineOwnershipPeriod> periodCaptor =
                ArgumentCaptor.forClass(BovineOwnershipPeriod.class);
        verify(savePeriodPort).save(periodCaptor.capture());
        assertThat(periodCaptor.getValue().farmId()).isSameAs(farm.id());
        assertThat(periodCaptor.getValue().farmId()).isNotSameAs(requestedFarmId);
    }

    @Test
    void shouldUseGeneratedBovineIdForInitialPeriod() {
        Farm farm = farmWithStatus(FarmStatus.ACTIVE, 10L);
        when(findFarmPort.findById(new FarmId(10L))).thenReturn(Optional.of(farm));

        service.create(commandFor(new FarmId(10L)));

        ArgumentCaptor<BovineOwnershipPeriod> periodCaptor =
                ArgumentCaptor.forClass(BovineOwnershipPeriod.class);
        verify(savePeriodPort).save(periodCaptor.capture());
        assertThat(periodCaptor.getValue().bovineId()).isEqualTo(BOVINE_ID);
    }

    private static CreateBovineWithInitialOwnershipCommand commandFor(FarmId farmId) {
        CreateBovineCommand bovineData = new CreateBovineCommand(
                "Mimosa",
                "BVL-26-0001",
                BovineSex.FEMALE,
                "Nelore",
                "Branca",
                LocalDate.of(2024, 1, 15),
                BovineStatus.ACTIVE,
                "TAG-001",
                null,
                null,
                BovineRegistrationCategory.UNREGISTERED,
                null,
                null,
                null,
                null,
                BOVINE_ORIGIN
        );
        return new CreateBovineWithInitialOwnershipCommand(bovineData, farmId);
    }

    private static Farm farmWithStatus(FarmStatus status, Long farmId) {
        FarmId id = new FarmId(farmId);
        return Farm.reconstitute(
                id,
                "Fazenda de teste",
                "TST",
                ZoneId.of("America/Sao_Paulo"),
                status,
                REGISTERED_AT.minusSeconds(3_600),
                REGISTERED_AT.minusSeconds(3_600)
        );
    }
}
