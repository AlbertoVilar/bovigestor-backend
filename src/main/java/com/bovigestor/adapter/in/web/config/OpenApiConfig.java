package com.bovigestor.adapter.in.web.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "BoviGestor API",
                version = "v1",
                description = "API REST para gestão de fazendas e rebanhos bovinos."
        )
)
public class OpenApiConfig {
}
