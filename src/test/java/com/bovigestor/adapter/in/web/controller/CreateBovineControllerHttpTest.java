package com.bovigestor.adapter.in.web.controller;

import com.bovigestor.adapter.in.web.exception.FarmWebExceptionHandler;
import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.application.port.in.CreateBovineUseCase;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineOrigin;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = CreateBovineController.class,
        properties = "bovigestor.bovine.creation.enabled=true"
)
@ActiveProfiles("dev")
@Import(FarmWebExceptionHandler.class)
class CreateBovineControllerHttpTest {

    private static final String ENDPOINT = "/api/v1/bovines";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateBovineUseCase createBovineUseCase;

    @Test
    void shouldDelegateValidRequestAndReturnOnlyCreatedBovineId() throws Exception {
        given(createBovineUseCase.create(any(CreateBovineCommand.class)))
                .willReturn(new BovineId(734L));

        MvcResult result = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(734L))
                .andReturn();

        var response = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(result.getResponse().getContentAsString());
        assertThat(response.size()).isEqualTo(1);

        var commandCaptor = org.mockito.ArgumentCaptor.forClass(CreateBovineCommand.class);
        verify(createBovineUseCase).create(commandCaptor.capture());
        assertThat(commandCaptor.getValue()).isEqualTo(new CreateBovineCommand(
                "Bovina Teste API",
                "BVL-26-985731",
                BovineSex.FEMALE,
                "Nelore",
                "Branca",
                LocalDate.of(2024, 5, 12),
                BovineStatus.ACTIVE,
                "BRINCO-985731",
                null,
                null,
                BovineRegistrationCategory.UNREGISTERED,
                null,
                null,
                null,
                null,
                new BovineOrigin("Ana Silva", "Fazenda Boa Vista", null)
        ));
    }

    @Test
    void shouldAcceptOriginNamesAtMaximumLength() throws Exception {
        given(createBovineUseCase.create(any(CreateBovineCommand.class)))
                .willReturn(new BovineId(735L));
        String request = validRequest()
                .replace("Ana Silva", "A".repeat(255))
                .replace("Fazenda Boa Vista", "F".repeat(255));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        var commandCaptor = org.mockito.ArgumentCaptor.forClass(CreateBovineCommand.class);
        verify(createBovineUseCase).create(commandCaptor.capture());
        assertThat(commandCaptor.getValue().origin().creatorName()).hasSize(255);
        assertThat(commandCaptor.getValue().origin().originFarmName()).hasSize(255);
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void shouldRejectBeanInvalidRequestsWithoutCallingUseCase(String body) throws Exception {
        MvcResult result = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("SQLException", "stackTrace", "INSERT INTO");
        verifyNoInteractions(createBovineUseCase);
    }

    @ParameterizedTest
    @MethodSource("invalidOriginRequests")
    void shouldRejectInvalidOriginWithoutCallingUseCase(String body) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createBovineUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenDomainRejectsBusinessInvariant() throws Exception {
        given(createBovineUseCase.create(any(CreateBovineCommand.class)))
                .willThrow(new IllegalArgumentException("internal domain detail"));

        MvcResult result = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains("Dados inválidos")
                .doesNotContain("internal domain detail");
        verify(createBovineUseCase, org.mockito.Mockito.times(1)).create(any());
    }

    @Test
    void shouldReturnConflictForDuplicateInternalCodeWithoutDatabaseDetails() throws Exception {
        given(createBovineUseCase.create(any(CreateBovineCommand.class)))
                .willThrow(new org.springframework.dao.DataIntegrityViolationException(
                        "duplicate internal code",
                        new SQLException("constraint uq_bovine_internal_code", "23505")
                ));

        MvcResult result = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isConflict())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains("A solicitação conflita com dados já existentes.")
                .doesNotContain("uq_bovine_internal_code", "duplicate internal code", "23505");
    }

    @Test
    void shouldReturnSanitizedInternalServerErrorForUnexpectedFailure() throws Exception {
        given(createBovineUseCase.create(any(CreateBovineCommand.class)))
                .willThrow(new IllegalStateException("internal database detail"));

        MvcResult result = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isInternalServerError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("internal database detail", "SQLException", "stackTrace");
    }

    @Test
    void shouldRejectMalformedJsonWithoutCallingUseCase() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bovina\",\"sex\": "))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createBovineUseCase);
    }

    @Test
    void shouldRejectMissingBodyWithoutCallingUseCase() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createBovineUseCase);
    }

    @Test
    void shouldReturnMethodNotAllowedForUnsupportedMethod() throws Exception {
        MvcResult result = mockMvc.perform(put(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("HttpRequestMethodNotSupportedException", "stackTrace");
        verifyNoInteractions(createBovineUseCase);
    }

    @Test
    void shouldReturnUnsupportedMediaTypeForUnsupportedContentType() throws Exception {
        MvcResult result = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=Bovina"))
                .andExpect(status().isUnsupportedMediaType())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("HttpMediaTypeNotSupportedException", "stackTrace");
        verifyNoInteractions(createBovineUseCase);
    }

    static Stream<Arguments> invalidRequests() {
        return Stream.of(
                Arguments.of("""
                        {"internalCode":"BVL-26-985731","sex":"FEMALE","status":"ACTIVE","registrationCategory":"UNREGISTERED"}
                        """),
                Arguments.of("""
                        {"name":"   ","internalCode":"BVL-26-985731","sex":"FEMALE","status":"ACTIVE","registrationCategory":"UNREGISTERED"}
                        """),
                Arguments.of("""
                        {"name":"Bovina","sex":"FEMALE","status":"ACTIVE","registrationCategory":"UNREGISTERED"}
                        """),
                Arguments.of("""
                        {"name":"Bovina","internalCode":"bvl-26-985731","sex":"FEMALE","status":"ACTIVE","registrationCategory":"UNREGISTERED"}
                        """),
                Arguments.of("""
                        {"name":"Bovina","internalCode":"BVL-26-985731","status":"ACTIVE","registrationCategory":"UNREGISTERED"}
                        """),
                Arguments.of("""
                        {"name":"Bovina","internalCode":"BVL-26-985731","sex":"UNKNOWN","status":"ACTIVE","registrationCategory":"UNREGISTERED"}
                        """),
                Arguments.of("""
                        {"name":"Bovina","internalCode":"BVL-26-985731","sex":"FEMALE","status":"UNKNOWN","registrationCategory":"UNREGISTERED"}
                        """),
                Arguments.of("""
                        {"name":"Bovina","internalCode":"BVL-26-985731","sex":"FEMALE","status":"ACTIVE"}
                        """),
                Arguments.of("""
                        {"name":"Bovina","internalCode":"BVL-26-985731","sex":"FEMALE","status":"ACTIVE","registrationCategory":"UNKNOWN"}
                        """),
                Arguments.of("""
                        {"name":"Bovina","internalCode":"BVL-26-985731","sex":"FEMALE","status":"ACTIVE","registrationCategory":"UNREGISTERED","fatherId":0}
                        """),
                Arguments.of("""
                        {"name":"Bovina","internalCode":"BVL-26-985731","sex":"FEMALE","status":"ACTIVE","registrationCategory":"UNREGISTERED","motherId":-1}
                        """),
                Arguments.of("""
                        {"name":"%s","internalCode":"BVL-26-985731","sex":"FEMALE","status":"ACTIVE","registrationCategory":"UNREGISTERED"}
                        """.formatted("a".repeat(256))),
                Arguments.of("""
                        {"name":"Bovina","internalCode":"BVL-26-985731","sex":"FEMALE","status":"ACTIVE","registrationCategory":"UNREGISTERED","birthDate":"not-a-date"}
                        """)
        ).map(arguments -> Arguments.of(withValidOrigin((String) arguments.get()[0])));
    }

    static Stream<Arguments> invalidOriginRequests() {
        return Stream.of(
                validRequest().replaceAll(
                        ",\\s*\"origin\"\\s*:\\s*\\{[^}]+\\}",
                        ""
                ),
                validRequest().replace("\"creatorName\": \"Ana Silva\"", "\"creatorName\": \"   \""),
                validRequest().replace("\"originFarmName\": \"Fazenda Boa Vista\"", "\"originFarmName\": \" \""),
                validRequest().replace("\"originFarmId\": null", "\"originFarmId\": 0"),
                validRequest().replace("Ana Silva", "A".repeat(256)),
                validRequest().replace("Fazenda Boa Vista", "F".repeat(256))
        ).map(Arguments::of);
    }

    private static String withValidOrigin(String body) {
        int closingBrace = body.lastIndexOf('}');
        return body.substring(0, closingBrace)
                + ",\"origin\":{\"creatorName\":\"Ana Silva\",\"originFarmName\":\"Fazenda Boa Vista\",\"originFarmId\":null}"
                + body.substring(closingBrace);
    }

    private static String validRequest() {
        return """
                {
                  "name": "Bovina Teste API",
                  "internalCode": "BVL-26-985731",
                  "sex": "FEMALE",
                  "breed": "Nelore",
                  "coatColor": "Branca",
                  "birthDate": "2024-05-12",
                  "status": "ACTIVE",
                  "earTag": "BRINCO-985731",
                  "registrationNumber": null,
                  "registrationAssociation": null,
                  "registrationCategory": "UNREGISTERED",
                  "fatherId": null,
                  "motherId": null,
                  "fatherRegistrationNumber": null,
                  "motherRegistrationNumber": null,
                  "origin": {"creatorName": "Ana Silva", "originFarmName": "Fazenda Boa Vista", "originFarmId": null}
                }
                """;
    }
}
