package com.bovigestor.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados necessários para cadastrar uma fazenda.")
public record FarmRequest(
        @Schema(
                description = "Nome da fazenda.",
                example = "Fazenda Boa Vista",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O nome da fazenda é obrigatório e não pode conter somente espaços")
        @Size(max = 255, message = "O nome da fazenda deve conter no máximo 255 caracteres")
        String name,

        @Schema(
                description = "Série exclusiva da fazenda: três ou quatro letras maiúsculas de A a Z.",
                example = "BVL",
                pattern = "^[A-Z]{3,4}$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "A série da fazenda é obrigatória")
        @Pattern(
                regexp = "[A-Z]{3,4}",
                message = "A série da fazenda deve conter três ou quatro letras maiúsculas"
        )
        String codeSeries
) {
}
