package com.bovigestor.adapter.in.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CreateFarmControllerIntegrationTest {

    private static final String FARMS_ENDPOINT = "/api/v1/farms";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearFarmData() {
        jdbcTemplate.update("DELETE FROM farm");
    }

    @Test
    void shouldPersistFarmThroughHttpAndReturnOnlyItsId() throws Exception {
        MvcResult result = mockMvc.perform(post(FARMS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Fazenda Integração HTTP","codeSeries":"HIT"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        JsonNode response = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertThat(response.size()).isEqualTo(1);

        Integer savedFarms = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM farm WHERE code_series = ?",
                Integer.class,
                "HIT"
        );
        assertThat(savedFarms).isEqualTo(1);
    }

    @Test
    void shouldReturnConflictForDuplicateCodeSeriesWithoutDatabaseDetails() throws Exception {
        String requestBody = """
                {"name":"Fazenda Original","codeSeries":"DUP"}
                """;

        mockMvc.perform(post(FARMS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        MvcResult duplicateResult = mockMvc.perform(post(FARMS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Outra Fazenda","codeSeries":"DUP"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail")
                        .value("A solicitação conflita com dados já existentes."))
                .andReturn();

        String responseBody = duplicateResult.getResponse().getContentAsString();
        assertThat(responseBody)
                .doesNotContain("uq_farm_code_series")
                .doesNotContain("JdbcSQL")
                .doesNotContain("SQLException")
                .doesNotContain("INSERT INTO")
                .doesNotContain("SQLState");
    }

    @Test
    void shouldExposeOpenApiDocumentationForFarmCreation() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode openApi = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertThat(openApi.path("info").path("title").asText()).isEqualTo("BoviGestor API");
        assertThat(openApi.at("/paths/~1api~1v1~1farms/post/responses/201").isObject()).isTrue();
        assertThat(openApi.at("/paths/~1api~1v1~1farms/post/responses/400").isObject()).isTrue();
        assertThat(openApi.at("/paths/~1api~1v1~1farms/post/responses/409").isObject()).isTrue();
        assertThat(openApi.at("/paths/~1api~1v1~1farms/post/responses/500").isObject()).isTrue();
    }
}
