package com.bovigestor.domain.model;

import java.time.LocalDate;

public record BovineRegistrationData(
        String name,
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
) {}
