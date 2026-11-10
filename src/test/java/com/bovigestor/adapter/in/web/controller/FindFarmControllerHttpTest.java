package com.bovigestor.adapter.in.web.controller;

import com.bovigestor.adapter.in.web.exception.FarmWebExceptionHandler;
import com.bovigestor.application.exceptions.ResourceNotFoundException;
import com.bovigestor.application.port.in.FindFarmByIdUseCase;
import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;
import com.bovigestor.domain.model.FarmStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.ZoneId;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FindFarmController.class)
@Import(FarmWebExceptionHandler.class)
class FindFarmControllerHttpTest {

    private static final String ENDPOINT = "/api/v1/farms/{id}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindFarmByIdUseCase findFarmByIdUseCase;

    @Test
    void shouldReturnFarmResponseAndCallUseCaseWithRequestedId() throws Exception {
        FarmId farmId = new FarmId(501L);
        given(findFarmByIdUseCase.findById(farmId)).willReturn(farm());

        mockMvc.perform(get(ENDPOINT, farmId.value()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(501L))
                .andExpect(jsonPath("$.name").value("Fazenda Santa Rita"))
                .andExpect(jsonPath("$.codeSeries").value("SRT"))
                .andExpect(jsonPath("$.timeZone").value("America/Manaus"))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T10:00:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-01-02T10:00:00Z"));

        verify(findFarmByIdUseCase).findById(farmId);
    }

    @Test
    void shouldReturnNotFoundProblemWhenFarmDoesNotExist() throws Exception {
        FarmId farmId = new FarmId(501L);
        given(findFarmByIdUseCase.findById(farmId))
                .willThrow(new ResourceNotFoundException("Fazenda não encontrada"));

        mockMvc.perform(get(ENDPOINT, farmId.value()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.detail").value("O recurso solicitado não foi encontrado."));

        verify(findFarmByIdUseCase).findById(farmId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "not-a-number"})
    void shouldReturnBadRequestForInvalidPathIdWithoutCallingUseCase(String id) throws Exception {
        mockMvc.perform(get("/api/v1/farms/{id}", id))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(findFarmByIdUseCase);
    }

    private static Farm farm() {
        return Farm.reconstitute(
                new FarmId(501L),
                "Fazenda Santa Rita",
                "SRT",
                ZoneId.of("America/Manaus"),
                FarmStatus.INACTIVE,
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-02T10:00:00Z")
        );
    }
}
