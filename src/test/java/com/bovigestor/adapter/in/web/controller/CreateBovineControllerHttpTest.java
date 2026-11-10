package com.bovigestor.adapter.in.web.controller;

import com.bovigestor.adapter.in.web.exception.FarmWebExceptionHandler;
import com.bovigestor.application.exceptions.InactiveFarmException;
import com.bovigestor.application.exceptions.ResourceNotFoundException;
import com.bovigestor.application.port.in.CreateBovineWithInitialOwnershipCommand;
import com.bovigestor.application.port.in.CreateBovineWithInitialOwnershipUseCase;
import com.bovigestor.domain.model.BovineId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CreateBovineController.class,
        properties = "bovigestor.bovine.creation.enabled=true")
@ActiveProfiles("dev")
@Import(FarmWebExceptionHandler.class)
class CreateBovineControllerHttpTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean CreateBovineWithInitialOwnershipUseCase useCase;

    @Test
    void shouldCreateWithInitialFarm() throws Exception {
        when(useCase.create(any())).thenReturn(new BovineId(101L));
        mockMvc.perform(post("/api/v1/bovines").contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(101));
        var captor = org.mockito.ArgumentCaptor.forClass(CreateBovineWithInitialOwnershipCommand.class);
        verify(useCase).create(captor.capture());
        assertThat(captor.getValue().farmId().value()).isEqualTo(15L);
        assertThat(captor.getValue().bovineData().origin().originFarmId()).isNull();
    }

    @Test
    void shouldRejectMissingAndInvalidFarmId() throws Exception {
        for (String body : new String[] {
                validRequest().replace("\"farmId\":15", "\"unused\":15"),
                validRequest().replace("\"farmId\":15", "\"farmId\":null"),
                validRequest().replace("\"farmId\":15", "\"farmId\":0")
        }) {
            mockMvc.perform(post("/api/v1/bovines").contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(useCase);
    }

    @Test
    void shouldReturnConflictForInactiveFarmWithoutInternalMessage() throws Exception {
        when(useCase.create(any())).thenThrow(new InactiveFarmException("internal detail"));
        var result = mockMvc.perform(post("/api/v1/bovines").contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isConflict()).andReturn();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("internal detail");
    }

    @Test
    void shouldReturnNotFoundForMissingFarm() throws Exception {
        when(useCase.create(any())).thenThrow(new ResourceNotFoundException("internal detail"));
        mockMvc.perform(post("/api/v1/bovines").contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isNotFound());
    }

    private static String validRequest() {
        return """
                {"name":"Mimosa","internalCode":"BVL-26-0001","sex":"FEMALE",
                 "status":"ACTIVE","registrationCategory":"UNREGISTERED",
                 "origin":{"creatorName":"Ana","originFarmName":"Fazenda origem","originFarmId":null},
                 "farmId":15}
                """;
    }
}
