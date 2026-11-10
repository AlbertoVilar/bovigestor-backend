package com.bovigestor.application.service;

import com.bovigestor.application.exceptions.ResourceNotFoundException;
import com.bovigestor.application.port.out.FindFarmPort;
import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;
import com.bovigestor.domain.model.FarmStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindFarmServiceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-01-02T10:00:00Z");

    @Mock
    private FindFarmPort findFarmPort;

    @InjectMocks
    private FindFarmService findFarmService;

    @Test
    void shouldReturnFarmWhenFound() {
        FarmId id = new FarmId(1L);
        Farm expectedFarm = createFarm(id, FarmStatus.ACTIVE);
        when(findFarmPort.findById(id)).thenReturn(Optional.of(expectedFarm));

        Farm result = findFarmService.findById(id);

        assertThat(result).isSameAs(expectedFarm);
        verify(findFarmPort).findById(id);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenFarmDoesNotExist() {
        FarmId id = new FarmId(2L);
        when(findFarmPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> findFarmService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(findFarmPort).findById(id);
    }

    @Test
    void shouldRejectNullFarmIdWithoutCallingPort() {
        assertThatThrownBy(() -> findFarmService.findById(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(findFarmPort);
    }

    @Test
    void shouldRejectNullPortInConstructor() {
        assertThatThrownBy(() -> new FindFarmService(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldPassExpectedFarmIdToPort() {
        FarmId id = new FarmId(3L);
        Farm expectedFarm = createFarm(id, FarmStatus.ACTIVE);
        when(findFarmPort.findById(id)).thenReturn(Optional.of(expectedFarm));

        findFarmService.findById(id);

        verify(findFarmPort).findById(id);
    }

    @Test
    void shouldReturnExistingInactiveFarmWithoutChangingItsStatus() {
        FarmId id = new FarmId(4L);
        Farm inactiveFarm = createFarm(id, FarmStatus.INACTIVE);
        when(findFarmPort.findById(id)).thenReturn(Optional.of(inactiveFarm));

        Farm result = findFarmService.findById(id);

        assertThat(result).isSameAs(inactiveFarm);
        assertThat(result.status()).isEqualTo(FarmStatus.INACTIVE);
        verify(findFarmPort).findById(id);
    }

    private static Farm createFarm(FarmId id, FarmStatus status) {
        return Farm.reconstitute(
                id,
                "Fazenda Boa Vista",
                "BVL",
                ZoneId.of("America/Sao_Paulo"),
                status,
                CREATED_AT,
                UPDATED_AT
        );
    }
}
