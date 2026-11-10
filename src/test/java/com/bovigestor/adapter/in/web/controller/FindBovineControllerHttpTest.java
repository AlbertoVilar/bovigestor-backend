package com.bovigestor.adapter.in.web.controller;

import com.bovigestor.adapter.in.web.exception.FarmWebExceptionHandler;
import com.bovigestor.application.exceptions.ResourceNotFoundException;
import com.bovigestor.application.port.in.FindBovineByIdUseCase;
import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineInternalCode;
import com.bovigestor.domain.model.BovineOrigin;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineRegistrationData;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import com.bovigestor.domain.model.FarmId;
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
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FindBovineController.class)
@Import(FarmWebExceptionHandler.class)
class FindBovineControllerHttpTest {

    private static final String ENDPOINT = "/api/v1/bovines/{id}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindBovineByIdUseCase findBovineByIdUseCase;

    @Test
    void shouldReturnBovineResponseAndCallUseCaseWithRequestedId() throws Exception {
        BovineId bovineId = new BovineId(501L);
        Bovine bovine = bovine();
        given(findBovineByIdUseCase.findById(bovineId)).willReturn(bovine);

        var result = mockMvc.perform(get(ENDPOINT, bovineId.value()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(501L))
                .andExpect(jsonPath("$.internalCode").value("BVL-26-0501"))
                .andExpect(jsonPath("$.name").value("Mimosa"))
                .andExpect(jsonPath("$.sex").value("FEMALE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.origin.creatorName").value("Ana Silva"))
                .andExpect(jsonPath("$.origin.originFarmName").value("Fazenda Boa Vista"))
                .andExpect(jsonPath("$.origin.originFarmId").value(15L))
                .andExpect(jsonPath("$.createdAt").value("2026-06-01T12:00:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-06-03T15:30:00Z"))
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains("\"fatherId\":201", "\"motherRegistrationNumber\":\"EXT-M-501\"");
        verify(findBovineByIdUseCase).findById(bovineId);
    }

    @Test
    void shouldReturnNotFoundProblemWhenBovineDoesNotExist() throws Exception {
        BovineId bovineId = new BovineId(501L);
        given(findBovineByIdUseCase.findById(bovineId))
                .willThrow(new ResourceNotFoundException(
                        "Bovino não encontrado para o identificador: 501"
                ));

        mockMvc.perform(get(ENDPOINT, bovineId.value()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"));

        verify(findBovineByIdUseCase).findById(bovineId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "not-a-number"})
    void shouldReturnBadRequestForInvalidPathIdWithoutCallingUseCase(String id) throws Exception {
        mockMvc.perform(get("/api/v1/bovines/{id}", id))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(findBovineByIdUseCase);
    }

    private static Bovine bovine() {
        Instant createdAt = Instant.parse("2026-06-01T12:00:00Z");
        Instant updatedAt = Instant.parse("2026-06-03T15:30:00Z");
        return Bovine.reconstitute(
                new BovineId(501L),
                new BovineInternalCode("BVL-26-0501"),
                new BovineRegistrationData(
                        "Mimosa",
                        BovineSex.FEMALE,
                        "Nelore",
                        "Branca",
                        LocalDate.of(2024, 5, 12),
                        BovineStatus.ACTIVE,
                        "BRINCO-501",
                        "REG-2024-501",
                        "ABC",
                        BovineRegistrationCategory.PO,
                        new BovineId(201L),
                        null,
                        "EXT-F-501",
                        "EXT-M-501"
                ),
                createdAt,
                updatedAt,
                new BovineOrigin("Ana Silva", "Fazenda Boa Vista", new FarmId(15L))
        );
    }
}
