package com.bovigestor.adapter.in.web.controller;

import com.bovigestor.BovigestorApplication;
import com.bovigestor.application.port.in.CreateBovineUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
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
import java.util.concurrent.atomic.AtomicInteger;
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
              "motherRegistrationNumber": null
            }
            """;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @ParameterizedTest(name = "profiles={0}, flag={2} -> controllerAvailable={3}")
    @MethodSource("profileScenarios")
    void shouldEnforceProfileAndFeatureFlagGuard(
            String scenario,
            String activeProfiles,
            boolean featureFlagEnabled,
            boolean expectedAvailable
    ) throws Exception {
        String databaseName = "bovine_profile_guard_" + scenario.replace('-', '_')
                + "_" + System.nanoTime();
        String databaseUrl = "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
        AtomicInteger useCaseCalls = new AtomicInteger();

        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(BovigestorApplication.class)
                .web(WebApplicationType.SERVLET)
                .profiles(activeProfiles.split(","))
                .initializers(applicationContext -> applicationContext.addBeanFactoryPostProcessor(beanFactory ->
                        beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
                            @Override
                            public Object postProcessAfterInitialization(Object bean, String beanName) {
                                if (bean instanceof CreateBovineUseCase useCase) {
                                    return (CreateBovineUseCase) command -> {
                                        useCaseCalls.incrementAndGet();
                                        return useCase.create(command);
                                    };
                                }
                                return bean;
                            }
                        })))
                .run(
                        "--spring.datasource.url=" + databaseUrl,
                        "--spring.datasource.username=sa",
                        "--spring.datasource.password=",
                        "--spring.datasource.driver-class-name=org.h2.Driver",
                        "--spring.flyway.enabled=true",
                        "--spring.jpa.hibernate.ddl-auto=validate",
                        "--springdoc.api-docs.enabled=true",
                        "--springdoc.swagger-ui.enabled=false",
                        "--bovigestor.bovine.creation.enabled=" + featureFlagEnabled,
                        "--server.address=127.0.0.1",
                        "--server.port=0",
                        "--spring.main.banner-mode=off"
                )) {
            boolean controllerRegistered = !context.getBeansOfType(CreateBovineController.class).isEmpty();
            assertThat(controllerRegistered)
                    .as("CreateBovineController registration for %s", activeProfiles)
                    .isEqualTo(expectedAvailable);

            var serverContext = (WebServerApplicationContext) context;
            URI baseUri = URI.create("http://127.0.0.1:" + serverContext.getWebServer().getPort());
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

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

            JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
            Integer persistedRows = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM bovine WHERE internal_code = ?",
                    Integer.class,
                    INTERNAL_CODE
            );
            assertThat(useCaseCalls.get()).isEqualTo(expectedAvailable ? 1 : 0);

            if (expectedAvailable) {
                assertThat(postResponse.statusCode()).isEqualTo(201);
                JsonNode response = OBJECT_MAPPER.readTree(postResponse.body());
                assertThat(response.path("id").asLong()).isPositive();
                assertThat(persistedRows).isEqualTo(1);
            } else {
                assertThat(postResponse.statusCode()).isEqualTo(404);
                assertThat(persistedRows).isZero();
            }
        }
    }

    private static Stream<Arguments> profileScenarios() {
        return Stream.of(
                Arguments.of("dev-enabled", "dev", true, true),
                Arguments.of("dev-disabled", "dev", false, false),
                Arguments.of("prod-enabled", "prod", true, false),
                Arguments.of("prod-dev-enabled", "prod,dev", true, false),
                Arguments.of("dev-prod-enabled", "dev,prod", true, false),
                Arguments.of("without-dev-enabled", "test", true, false)
        );
    }
}
