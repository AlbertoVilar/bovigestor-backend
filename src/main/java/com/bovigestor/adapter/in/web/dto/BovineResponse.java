package com.bovigestor.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Representação cadastral de um bovino para operações de consulta.")
public record BovineResponse(
        @Schema(description = "Identificador do bovino.", example = "123")
        Long id,

        @Schema(description = "Código interno imutável do bovino.", example = "BVL-26-985731")
        String internalCode,

        @Schema(description = "Nome do bovino.", example = "Mimosa")
        String name,

        @Schema(description = "Sexo do bovino.", example = "FEMALE")
        String sex,

        @Schema(description = "Raça do bovino.", example = "Nelore")
        String breed,

        @Schema(description = "Pelagem do bovino.", example = "Branca")
        String coatColor,

        @Schema(description = "Data de nascimento do bovino.", example = "2024-05-12")
        LocalDate birthDate,

        @Schema(description = "Situação cadastral do bovino.", example = "ACTIVE")
        String status,

        @Schema(description = "Identificação física, como brinco.", example = "BRINCO-985731")
        String earTag,

        @Schema(description = "Número de registro genealógico.", example = "REG-2024-985731")
        String registrationNumber,

        @Schema(description = "Associação de registro genealógico.", example = "ABC")
        String registrationAssociation,

        @Schema(description = "Categoria de registro genealógico.", example = "PO")
        String registrationCategory,

        @Schema(description = "Identificador local do pai, quando cadastrado.", example = "120")
        Long fatherId,

        @Schema(description = "Identificador local da mãe, quando cadastrada.", example = "121")
        Long motherId,

        @Schema(description = "Referência genealógica externa do pai.", example = "EXT-F-985731")
        String fatherRegistrationNumber,

        @Schema(description = "Referência genealógica externa da mãe.", example = "EXT-M-985731")
        String motherRegistrationNumber,

        @Schema(description = "Instante UTC de criação do cadastro.", example = "2026-06-01T12:00:00Z")
        Instant createdAt,

        @Schema(description = "Instante UTC da última atualização do cadastro.", example = "2026-06-03T15:30:00Z")
        Instant updatedAt,

        @Schema(description = "Origem cadastral informada para o bovino.")
        OriginResponse origin
) {
    public record OriginResponse(
            @Schema(description = "Nome informado do criador.", example = "Ana Silva")
            String creatorName,

            @Schema(description = "Nome informado da fazenda de origem.", example = "Fazenda Boa Vista")
            String originFarmName,

            @Schema(description = "Identificador local da fazenda de origem, quando cadastrada.", example = "15")
            Long originFarmId
    ) {
    }
}
