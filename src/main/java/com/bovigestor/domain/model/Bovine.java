package com.bovigestor.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

public class Bovine {
    private final BovineId id;
    private final BovineInternalCode internalCode;
    private String name;
    private BovineSex sex;
    private String breed;
    private String coatColor;
    private LocalDate birthDate;
    private BovineStatus status;
    private String earTag;
    private String registrationNumber;
    private String registrationAssociation;
    private BovineRegistrationCategory registrationCategory;
    private BovineId fatherId;
    private BovineId motherId;
    private String fatherRegistrationNumber;
    private String motherRegistrationNumber;
    private final Instant createdAt;
    private Instant updatedAt;

    private Bovine(
            BovineId id,
            BovineInternalCode internalCode,
            BovineRegistrationData data,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "O identificador do bovino é obrigatório");
        this.internalCode = Objects.requireNonNull(
                internalCode,
                "O código interno do bovino é obrigatório"
        );
        Objects.requireNonNull(data, "Os dados de cadastro do bovino são obrigatórios");
        this.createdAt = Objects.requireNonNull(
                createdAt,
                "A data de criação é obrigatória"
        );

        this.name = validateName(data.name());
        this.sex = Objects.requireNonNull(data.sex(), "O sexo do bovino é obrigatório");
        this.breed = data.breed();
        this.coatColor = data.coatColor();
        this.birthDate = data.birthDate();
        this.status = Objects.requireNonNull(data.status(), "O status do bovino é obrigatório");
        this.earTag = data.earTag();
        this.registrationNumber = data.registrationNumber();
        this.registrationAssociation = data.registrationAssociation();
        this.registrationCategory = Objects.requireNonNull(
                data.registrationCategory(),
                "A categoria de registro do bovino é obrigatória"
        );
        this.fatherId = data.fatherId();
        this.motherId = data.motherId();
        this.fatherRegistrationNumber = data.fatherRegistrationNumber();
        this.motherRegistrationNumber = data.motherRegistrationNumber();
        this.updatedAt = createdAt;

        validateBirthDate(birthDate, createdAt);
        validateParentIds(id, fatherId, motherId);
        validateParentage(
                registrationCategory,
                fatherId,
                motherId,
                fatherRegistrationNumber,
                motherRegistrationNumber
        );
        validateRegistrationDetails(
                registrationCategory,
                registrationNumber,
                registrationAssociation
        );
    }

    public static Bovine createManually(
            BovineId id,
            BovineInternalCode internalCode,
            BovineRegistrationData data,
            Instant createdAt
    ) {
        return new Bovine(id, internalCode, data, createdAt);
    }

    public BovineId id() {
        return id;
    }

    public BovineInternalCode internalCode() {
        return internalCode;
    }

    public String name() {
        return name;
    }

    public BovineSex sex() {
        return sex;
    }

    public String breed() {
        return breed;
    }

    public String coatColor() {
        return coatColor;
    }

    public LocalDate birthDate() {
        return birthDate;
    }

    public BovineStatus status() {
        return status;
    }

    public String earTag() {
        return earTag;
    }

    public String registrationNumber() {
        return registrationNumber;
    }

    public String registrationAssociation() {
        return registrationAssociation;
    }

    public BovineRegistrationCategory registrationCategory() {
        return registrationCategory;
    }

    public BovineId fatherId() {
        return fatherId;
    }

    public BovineId motherId() {
        return motherId;
    }

    public String fatherRegistrationNumber() {
        return fatherRegistrationNumber;
    }

    public String motherRegistrationNumber() {
        return motherRegistrationNumber;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private static String validateName(String name) {
        Objects.requireNonNull(name, "O nome do bovino é obrigatório");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do bovino não pode estar vazio ou conter somente espaços"
            );
        }

        return name;
    }

    private static void validateBirthDate(LocalDate birthDate, Instant createdAt) {
        if (birthDate != null
                && birthDate.isAfter(createdAt.atZone(ZoneOffset.UTC).toLocalDate())) {
            throw new IllegalArgumentException(
                    "A data de nascimento não pode ser posterior à data de criação"
            );
        }
    }

    private static void validateParentIds(
            BovineId id,
            BovineId fatherId,
            BovineId motherId
    ) {
        if (id.equals(fatherId)) {
            throw new IllegalArgumentException(
                    "O identificador do pai não pode ser igual ao do próprio bovino"
            );
        }

        if (id.equals(motherId)) {
            throw new IllegalArgumentException(
                    "O identificador da mãe não pode ser igual ao do próprio bovino"
            );
        }
    }

    private static void validateRegistrationDetails(
            BovineRegistrationCategory category,
            String registrationNumber,
            String registrationAssociation
    ) {
        if (category == BovineRegistrationCategory.PO
                || category == BovineRegistrationCategory.PC
                || category == BovineRegistrationCategory.PA) {
            requireNonBlankRegistrationDetails(registrationNumber, registrationAssociation);
        }

        if (category == BovineRegistrationCategory.UNREGISTERED
                && (hasText(registrationNumber) || hasText(registrationAssociation))) {
            throw new IllegalArgumentException(
                    "A categoria sem registro oficial não permite número ou associação genealógica"
            );
        }
    }

    private static void requireNonBlankRegistrationDetails(
            String registrationNumber,
            String registrationAssociation
    ) {
        if (!hasText(registrationNumber)) {
            throw new IllegalArgumentException(
                    "O número de registro genealógico é obrigatório para a categoria informada"
            );
        }

        if (!hasText(registrationAssociation)) {
            throw new IllegalArgumentException(
                    "A associação de registro genealógico é obrigatória para a categoria informada"
            );
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static void validateParentage(
            BovineRegistrationCategory category,
            BovineId fatherId,
            BovineId motherId,
            String fatherRegistrationNumber,
            String motherRegistrationNumber
    ) {
        if (!requiresParentage(category)) {
            return;
        }

        if (!hasParentReference(fatherId, fatherRegistrationNumber)) {
            throw new IllegalArgumentException(
                    "Para bovinos PO ou PC, a referência do pai é obrigatória"
            );
        }

        if (!hasParentReference(motherId, motherRegistrationNumber)) {
            throw new IllegalArgumentException(
                    "Para bovinos PO ou PC, a referência da mãe é obrigatória"
            );
        }
    }

    private static boolean requiresParentage(BovineRegistrationCategory category) {
        return category == BovineRegistrationCategory.PO
                || category == BovineRegistrationCategory.PC;
    }

    private static boolean hasParentReference(BovineId parentId, String registrationNumber) {
        return parentId != null || hasText(registrationNumber);
    }

}
