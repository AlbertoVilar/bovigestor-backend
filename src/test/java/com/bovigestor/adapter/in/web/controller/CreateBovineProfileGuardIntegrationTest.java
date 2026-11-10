package com.bovigestor.adapter.in.web.controller;

import com.bovigestor.BovigestorApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CreateBovineProfileGuardIntegrationTest {

    private static final String INTERNAL_CODE = "BVL-26-987654321";
    private static final String REQUEST_JSON = """
            {
              "name": "Bovina Perfil Teste",
              "internalCode": "BVL-26-987654321",
              "sex": "FEMALE",
              "breed": "Nelore",
              "coatColor": "Branca",
              "birthDate": "2024-05-12",
              "status": "ACTIVE",
              "earTag": "BRINCO-PERFIL-987654321",
              "registrationNumber": null,
              "registrationAssociation": null,
              "registrationCategory": "UNREGISTERED",
              "fatherId": null,
              "motherId": null,
              "fatherRegistrationNumber": null,
              "motherRegistrationNumber": null,
              "origin": {"creatorName": "Ana Silva", "originFarmName": "Fazenda Boa Vista", "originFarmId": null},
              "farmId": 15
            }
            """;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @ParameterizedTest(name = "profiles={0}, creationFlag={2}")
    @MethodSource("profileScenarios")
    void shouldRestrictBovineCreationToEnabledDevelopmentProfile(
            String scenario,
            String activeProfiles,
            boolean featureFlagEnabled
    ) throws Exception {
        String databaseName = "bovine_profile_guard_" + scenario.replace('-', '_')
                + "_" + System.nanoTime();
        String databaseUrl = "jdbc:tc:postgresql:17:///" + databaseName;
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(BovigestorApplication.class)
                .web(WebApplicationType.SERVLET)
                .profiles(activeProfiles.split(","))
                .run(
                        "--spring.datasource.url=" + databaseUrl,
                        "--spring.datasource.username=sa",
                        "--spring.datasource.password=",
                        "--spring.flyway.enabled=true",
                        "--spring.jpa.hibernate.ddl-auto=validate",
                        "--springdoc.api-docs.enabled=true",
                        "--springdoc.swagger-ui.enabled=false",
                        "--bovigestor.bovine.creation.enabled=" + featureFlagEnabled,
                        "--server.address=127.0.0.1",
                        "--server.port=0",
                        "--spring.main.banner-mode=off"
                )) {
            var serverContext = (WebServerApplicationContext) context;
            URI baseUri = URI.create("http://127.0.0.1:" + serverContext.getWebServer().getPort());
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
            jdbcTemplate.update("INSERT INTO farm (id, name, code_series, time_zone, status, created_at, updated_at) "
                    + "VALUES (15, 'Fazenda teste', 'TST', 'America/Sao_Paulo', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            boolean expectedAvailable = activeProfiles.equals("dev") && featureFlagEnabled;

            HttpResponse<String> openApiResponse = client.send(
                    HttpRequest.newBuilder(baseUri.resolve("/v3/api-docs"))
                            .timeout(Duration.ofSeconds(10))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            assertThat(openApiResponse.statusCode()).isEqualTo(200);
            JsonNode openApi = OBJECT_MAPPER.readTree(openApiResponse.body());
            boolean operationDocumented = openApi.path("paths")
                    .path("/api/v1/bovines")
                    .has("post");
            assertThat(operationDocumented)
                    .as("POST OpenAPI documentation for %s", activeProfiles)
                    .isEqualTo(expectedAvailable);

            HttpResponse<String> postResponse = client.send(
                    HttpRequest.newBuilder(baseUri.resolve("/api/v1/bovines"))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(REQUEST_JSON))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            Integer persistedRows = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM bovine WHERE internal_code = ?",
                    Integer.class,
                    INTERNAL_CODE
            );
            assertThat(postResponse.statusCode()).isEqualTo(expectedAvailable ? 201 : 404);
            assertThat(persistedRows).isEqualTo(expectedAvailable ? 1 : 0);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM bovine_ownership_period WHERE farm_id = 15", Integer.class
            )).isEqualTo(expectedAvailable ? 1 : 0);
        }
    }

    private static Stream<Arguments> profileScenarios() {
        return Stream.of(
                Arguments.of("dev-enabled", "dev", true),
                Arguments.of("dev-disabled", "dev", false),
                Arguments.of("prod-enabled", "prod", true),
                Arguments.of("prod-dev-enabled", "prod,dev", true),
                Arguments.of("dev-prod-enabled", "dev,prod", true),
                Arguments.of("without-dev-enabled", "test", true)
        );
    }
}
