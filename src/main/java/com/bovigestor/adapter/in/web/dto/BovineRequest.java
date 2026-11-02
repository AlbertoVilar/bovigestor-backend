package com.bovigestor.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

import java.time.LocalDate;

@Schema(description = "Dados recebidos para cadastrar um bovino.")
public record BovineRequest(
        @Schema(description = "Nome do bovino.", example = "Bovina Teste API", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome do bovino é obrigatório e não pode conter somente espaços")
        @Size(max = 255, message = "O nome do bovino deve conter no máximo 255 caracteres")
        String name,

        @Schema(
                description = "Identificador interno imutável, único no BoviGestor.",
                example = "BVL-26-985731",
                pattern = "^[A-Z]{3,4}-[0-9]{2}-(?=[0-9]*[1-9])[0-9]{4,}$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O código interno do bovino é obrigatório")
        @Size(max = 255, message = "O código interno do bovino deve conter no máximo 255 caracteres")
        @Pattern(
                regexp = "[A-Z]{3,4}-[0-9]{2}-(?=[0-9]*[1-9])[0-9]{4,}",
                message = "O código interno deve seguir o formato SERIE-AA-SEQUENCIA"
        )
        String internalCode,

        @Schema(description = "Sexo do bovino.", example = "FEMALE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O sexo do bovino é obrigatório")
        BovineSex sex,

        @Schema(description = "Raça do bovino.", example = "Nelore")
        @Size(max = 255, message = "A raça deve conter no máximo 255 caracteres")
        String breed,

        @Schema(description = "Pelagem do bovino.", example = "Branca")
        @Size(max = 255, message = "A pelagem deve conter no máximo 255 caracteres")
        String coatColor,

        @Schema(description = "Data de nascimento do bovino.", example = "2024-05-12")
        LocalDate birthDate,

        @Schema(description = "Status cadastral.", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O status do bovino é obrigatório")
        BovineStatus status,

        @Schema(description = "Identificação física, como brinco.", example = "BRINCO-985731")
        @Size(max = 255, message = "A identificação física deve conter no máximo 255 caracteres")
        String earTag,

        @Schema(description = "Número de registro genealógico.", example = "REG-2024-985731")
        @Size(max = 255, message = "O número de registro deve conter no máximo 255 caracteres")
        String registrationNumber,

        @Schema(description = "Associação de registro genealógico.", example = "ABC")
        @Size(max = 255, message = "A associação de registro deve conter no máximo 255 caracteres")
        String registrationAssociation,

        @Schema(description = "Categoria de registro genealógico.", example = "UNREGISTERED", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "A categoria de registro do bovino é obrigatória")
        BovineRegistrationCategory registrationCategory,

        @Schema(description = "ID técnico do pai já cadastrado; não informe um ID inexistente.", example = "120")
        @Positive(message = "O identificador do pai deve ser positivo")
        Long fatherId,

        @Schema(description = "ID técnico da mãe já cadastrada; não informe um ID inexistente.", example = "121")
        @Positive(message = "O identificador da mãe deve ser positivo")
        Long motherId,

        @Schema(description = "Número genealógico externo do pai, quando não cadastrado localmente.", example = "EXT-F-985731")
        @Size(max = 255, message = "A referência externa do pai deve conter no máximo 255 caracteres")
        String fatherRegistrationNumber,

        @Schema(description = "Número genealógico externo da mãe, quando não cadastrada localmente.", example = "EXT-M-985731")
        @Size(max = 255, message = "A referência externa da mãe deve conter no máximo 255 caracteres")
        String motherRegistrationNumber,

        @Schema(description = "Origem cadastral do bovino.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "A origem do bovino é obrigatória")
        @Valid
        OriginRequest origin
) {
    public record OriginRequest(
            @Schema(description = "Nome informado do criador.", example = "José Pereira", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "O nome do criador é obrigatório")
            @Size(max = 255, message = "O nome do criador deve conter no máximo 255 caracteres")
            String creatorName,

            @Schema(description = "Nome informado da fazenda de origem.", example = "Fazenda Santa Rita", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "O nome da fazenda de origem é obrigatório")
            @Size(max = 255, message = "O nome da fazenda de origem deve conter no máximo 255 caracteres")
            String originFarmName,

            @Schema(description = "ID local da fazenda de origem, quando cadastrada.", example = "15")
            @Positive(message = "O identificador da fazenda de origem deve ser positivo")
            Long originFarmId
    ) {
    }
}
