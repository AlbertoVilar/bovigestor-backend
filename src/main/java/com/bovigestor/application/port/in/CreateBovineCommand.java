package com.bovigestor.application.port.in;

import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;

import java.time.LocalDate;
import java.util.Objects;

public record CreateBovineCommand(
        String name,
        String internalCode,
        BovineSex sex,
        String breed,
        String coatColor,
        LocalDate birthDate,
        BovineStatus status,
        String earTag,
        String registrationNumber,
        String registrationAssociation,
        BovineRegistrationCategory registrationCategory,
        BovineId fatherId,
        BovineId motherId,
        String fatherRegistrationNumber,
        String motherRegistrationNumber
) {
    public CreateBovineCommand {
        Objects.requireNonNull(name, "O nome do bovino é obrigatório");
        Objects.requireNonNull(internalCode, "O código interno do bovino é obrigatório");
        Objects.requireNonNull(sex, "O sexo do bovino é obrigatório");
        Objects.requireNonNull(status, "O status do bovino é obrigatório");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do bovino não pode estar vazio ou conter somente espaços"
            );
        }

        if (internalCode.isBlank()) {
            throw new IllegalArgumentException(
                    "O código interno do bovino não pode estar vazio ou conter somente espaços"
            );
        }
    }
}
