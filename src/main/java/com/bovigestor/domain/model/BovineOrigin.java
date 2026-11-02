package com.bovigestor.domain.model;

public record BovineOrigin(
        String creatorName,
        String originFarmName,
        FarmId originFarmId
) {
    public BovineOrigin {
        if (creatorName == null || creatorName.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do criador é obrigatório"
            );
        }

        if (originFarmName == null || originFarmName.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da fazenda de origem é obrigatório"
            );
        }

        creatorName = creatorName.strip();
        originFarmName = originFarmName.strip();

        if (creatorName.length() > 255) {
            throw new IllegalArgumentException(
                    "O nome do criador deve conter no máximo 255 caracteres"
            );
        }

        if (originFarmName.length() > 255) {
            throw new IllegalArgumentException(
                    "O nome da fazenda de origem deve conter no máximo 255 caracteres"
            );
        }
    }
}
