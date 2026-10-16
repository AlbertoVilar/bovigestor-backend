package com.bovigestor.adapter.in.web.controller;

import com.bovigestor.adapter.in.web.exception.FarmWebExceptionHandler;
import com.bovigestor.application.port.in.CreateFarmCommand;
import com.bovigestor.application.port.in.CreateFarmUseCase;
import com.bovigestor.domain.model.FarmId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreateFarmController.class)
@Import(FarmWebExceptionHandler.class)
class CreateFarmControllerHttpTest {

    private static final String FARMS_ENDPOINT = "/api/v1/farms";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateFarmUseCase createFarmUseCase;

    @Test
    void shouldDelegateValidRequestAndReturnOnlyCreatedFarmId() throws Exception {
        given(createFarmUseCase.create(any(CreateFarmCommand.class)))
                .willReturn(new FarmId(734L));

        MvcResult result = mockMvc.perform(post(FARMS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Fazenda Boa Vista","codeSeries":"BVL"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        var response = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(result.getResponse().getContentAsString());
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get("id").asLong()).isEqualTo(734L);

        var commandCaptor = org.mockito.ArgumentCaptor.forClass(CreateFarmCommand.class);
        verify(createFarmUseCase).create(commandCaptor.capture());
        assertThat(commandCaptor.getValue().name()).isEqualTo("Fazenda Boa Vista");
        assertThat(commandCaptor.getValue().codeSeries()).isEqualTo("BVL");
    }

    @ParameterizedTest
    @MethodSource("invalidFarmRequests")
    void shouldRejectInvalidFieldsWithBadRequest(String requestBody) throws Exception {
        mockMvc.perform(post(FARMS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createFarmUseCase);
    }

    @Test
    void shouldRejectFarmNameLongerThan255CharactersWithBadRequest() throws Exception {
        String requestBody = "{\"name\":\"" + "a".repeat(256)
                + "\",\"codeSeries\":\"BVL\"}";

        mockMvc.perform(post(FARMS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createFarmUseCase);
    }

    @Test
    void shouldReturnMethodNotAllowedForUnsupportedHttpMethod() throws Exception {
        MvcResult result = mockMvc.perform(put(FARMS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("HttpRequestMethodNotSupportedException")
                .doesNotContain("stackTrace");
        verifyNoInteractions(createFarmUseCase);
    }

    @Test
    void shouldReturnUnsupportedMediaTypeForUnsupportedContentType() throws Exception {
        MvcResult result = mockMvc.perform(post(FARMS_ENDPOINT)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=Fazenda&codeSeries=BVL"))
                .andExpect(status().isUnsupportedMediaType())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("HttpMediaTypeNotSupportedException")
                .doesNotContain("stackTrace");
        verifyNoInteractions(createFarmUseCase);
    }

    static Stream<Arguments> invalidFarmRequests() {
        return Stream.of(
                Arguments.of("{\"codeSeries\":\"BVL\"}"),
                Arguments.of("{\"name\":null,\"codeSeries\":\"BVL\"}"),
                Arguments.of("{\"name\":\"   \",\"codeSeries\":\"BVL\"}"),
                Arguments.of("{\"name\":\"Fazenda\"}"),
                Arguments.of("{\"name\":\"Fazenda\",\"codeSeries\":null}"),
                Arguments.of("{\"name\":\"Fazenda\",\"codeSeries\":\"  \"}"),
                Arguments.of("{\"name\":\"Fazenda\",\"codeSeries\":\"bvl\"}"),
                Arguments.of("{\"name\":\"Fazenda\",\"codeSeries\":\"AB\"}"),
                Arguments.of("{\"name\":\"Fazenda\",\"codeSeries\":\"ABCDE\"}"),
                Arguments.of("{\"name\":\"Fazenda\",\"codeSeries\":\"AB1\"}")
        );
    }

    @Test
    void shouldRejectMalformedJsonWithBadRequest() throws Exception {
        mockMvc.perform(post(FARMS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Fazenda\",\"codeSeries\":"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createFarmUseCase);
    }

    @Test
    void shouldRejectJsonWithIncompatibleFieldTypeWithBadRequest() throws Exception {
        mockMvc.perform(post(FARMS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":{\"value\":\"Fazenda\"},\"codeSeries\":\"BVL\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createFarmUseCase);
    }

    @Test
    void shouldRejectMissingBodyWithBadRequest() throws Exception {
        mockMvc.perform(post(FARMS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createFarmUseCase);
    }

    @Test
    void shouldReturnSanitizedInternalServerErrorForUnexpectedFailure() throws Exception {
        given(createFarmUseCase.create(any(CreateFarmCommand.class)))
                .willThrow(new IllegalStateException("internal database detail"));

        MvcResult result = mockMvc.perform(post(FARMS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Fazenda Boa Vista","codeSeries":"BVL"}
                                """))
                .andExpect(status().isInternalServerError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("internal database detail")
                .doesNotContain("SQLException")
                .doesNotContain("stackTrace");
    }
}
