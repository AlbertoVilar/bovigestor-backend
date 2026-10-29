package com.bovigestor.adapter.in.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class CreateBovineHttpPostgresIntegrationTest {

    private static final AtomicLong CODE_SEQUENCE = new AtomicLong(90_000L);
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17")
    ).withDatabaseName("bovigestor_web_validation")
            .withUsername("bovine_web_test")
            .withPassword("bovine_web_test_password");

    @Container
    private static final PostgreSQLContainer POSTGRES_CONTAINER = POSTGRES;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Test
    void shouldCreateBovineThroughHttpPersistAllFieldsAndDocumentDevRestriction() throws Exception {
        assertThat(Arrays.stream(flyway.info().applied())
                .map(info -> info.getVersion().getVersion())
                .toList())
                .containsExactly("1", "2", "3");
        assertThat(jdbcTemplate.queryForObject("SELECT version()", String.class))
                .startsWith("PostgreSQL 17");
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM bovine", Integer.class))
                .isZero();

        String internalCode = nextInternalCode();
        String requestBody = request(internalCode);
        MvcResult createResult = mockMvc.perform(post("/api/v1/bovines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        JsonNode response = new ObjectMapper().readTree(createResult.getResponse().getContentAsString());
        assertThat(response.size()).isEqualTo(1);
        long id = response.path("id").asLong();

        Map<String, Object> saved = jdbcTemplate.queryForMap(
                "SELECT id, internal_code, name, sex, breed, coat_color, birth_date, status, "
                        + "ear_tag, registration_number, registration_association, registration_category, "
                        + "father_id, mother_id, father_registration_number, mother_registration_number, "
                        + "created_at, updated_at, origin_creator_name, origin_farm_name, origin_farm_id "
                        + "FROM bovine WHERE id = ?",
                id
        );
        assertThat(saved)
                .containsEntry("id", id)
                .containsEntry("internal_code", internalCode)
                .containsEntry("name", "Bovina Teste API")
                .containsEntry("sex", "FEMALE")
                .containsEntry("breed", "Nelore")
                .containsEntry("coat_color", "Branca")
                .containsEntry("birth_date", java.sql.Date.valueOf("2024-05-12"))
                .containsEntry("status", "ACTIVE")
                .containsEntry("ear_tag", "BRINCO-" + internalCode.substring(internalCode.lastIndexOf('-') + 1))
                .containsEntry("registration_number", null)
                .containsEntry("registration_association", null)
                .containsEntry("registration_category", "UNREGISTERED")
                .containsEntry("father_id", null)
                .containsEntry("mother_id", null)
                .containsEntry("father_registration_number", null)
                .containsEntry("mother_registration_number", null);
        assertThat(saved)
                .containsEntry("origin_creator_name", "Ana Silva")
                .containsEntry("origin_farm_name", "Fazenda Boa Vista")
                .containsEntry("origin_farm_id", null);
        assertThat(saved.get("created_at")).isInstanceOf(java.sql.Timestamp.class);
        assertThat(((java.sql.Timestamp) saved.get("created_at")).toInstant()).isNotNull();
        assertThat(saved.get("updated_at")).isEqualTo(saved.get("created_at"));
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bovine WHERE internal_code = ?",
                Integer.class,
                internalCode
        )).isEqualTo(1);

        mockMvc.perform(post("/api/v1/bovines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bovine WHERE internal_code = ?",
                Integer.class,
                internalCode
        )).isEqualTo(1);

        JsonNode openApi = new ObjectMapper().readTree(mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
        assertThat(openApi.at("/paths/~1api~1v1~1bovines/post/responses/201").isObject()).isTrue();
        assertThat(openApi.at("/paths/~1api~1v1~1bovines/post/responses/400").isObject()).isTrue();
        assertThat(openApi.at("/paths/~1api~1v1~1bovines/post/responses/409").isObject()).isTrue();
        assertThat(openApi.at("/paths/~1api~1v1~1bovines/post/responses/500").isObject()).isTrue();
        assertThat(openApi.at("/paths/~1api~1v1~1bovines/post/description").asText())
                .contains("perfil dev", "não cria titularidade inicial");
        assertThat(openApi.at("/components/schemas/BovineRequest/properties/sex/enum").toString())
                .contains("FEMALE", "MALE");
    }

    @Test
    void shouldReturnBadRequestWhenLocalParentDoesNotExist() throws Exception {
        String requestBody = request(nextInternalCode()).replace(
                "\"fatherId\": null",
                "\"fatherId\": 999999999"
        );

        mockMvc.perform(post("/api/v1/bovines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Uma referência local informada não existe."));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bovine WHERE internal_code = ?",
                Integer.class,
                extractInternalCode(requestBody)
        )).isZero();
    }

    private static String nextInternalCode() {
        return "BVT-26-%08d".formatted(CODE_SEQUENCE.getAndIncrement());
    }

    private static String request(String internalCode) {
        String sequence = internalCode.substring(internalCode.lastIndexOf('-') + 1);
        return """
                {
                  "name": "Bovina Teste API",
                  "internalCode": "%s",
                  "sex": "FEMALE",
                  "breed": "Nelore",
                  "coatColor": "Branca",
                  "birthDate": "2024-05-12",
                  "status": "ACTIVE",
                  "earTag": "BRINCO-%s",
                  "registrationNumber": null,
                  "registrationAssociation": null,
                  "registrationCategory": "UNREGISTERED",
                  "fatherId": null,
                  "motherId": null,
                  "fatherRegistrationNumber": null,
                  "motherRegistrationNumber": null,
                  "origin": {"creatorName": "Ana Silva", "originFarmName": "Fazenda Boa Vista", "originFarmId": null}
                }
                """.formatted(internalCode, sequence);
    }

    private static String extractInternalCode(String requestBody) {
        return requestBody.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("\"internalCode\""))
                .map(line -> line.split("\"", 4)[3])
                .findFirst()
                .orElseThrow();
    }
}
