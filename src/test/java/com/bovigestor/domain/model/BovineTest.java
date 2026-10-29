package com.bovigestor.domain.model;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BovineTest {

    private static final BovineId BOVINE_ID = new BovineId(1L);
    private static final BovineInternalCode INTERNAL_CODE =
            new BovineInternalCode("BVL-26-0001");
    private static final Instant CREATED_AT = Instant.parse("2026-06-01T23:30:00Z");
    private static final BovineOrigin BOVINE_ORIGIN = new BovineOrigin(
            "Criador de teste",
            "Fazenda de teste",
            null
    );

    @Test
    void shouldPreserveOriginAndExposeItThroughAccessor() {
        Bovine bovine = createBovine(registrationData().build());

        assertThat(bovine.origin()).isSameAs(BOVINE_ORIGIN);
    }

    @Test
    void shouldRejectNullOrigin() {
        assertThatThrownBy(() -> createBovine(
                BOVINE_ID,
                INTERNAL_CODE,
                registrationData().build(),
                CREATED_AT,
                null
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("A origem do bovino é obrigatória");
    }

    @Test
    void shouldCreatePaBovineWithoutParentReferences() {
        BovineRegistrationData data = registrationData().build();

        assertThat(createBovine(data)).isNotNull();
    }

    @Test
    void shouldCreatePoBovineWithExternalParentReferences() {
        BovineRegistrationData data = registeredData(BovineRegistrationCategory.PO)
                .withFatherRegistrationNumber("EXT-F-001")
                .withMotherRegistrationNumber("EXT-M-001")
                .build();

        assertThat(createBovine(data)).isNotNull();
    }

    @Test
    void shouldCreatePcBovineWithLocalParentReferences() {
        BovineRegistrationData data = registeredData(BovineRegistrationCategory.PC)
                .withFatherId(new BovineId(2L))
                .withMotherId(new BovineId(3L))
                .withoutFatherRegistrationNumber()
                .withoutMotherRegistrationNumber()
                .build();

        assertThat(createBovine(data)).isNotNull();
    }

    @Test
    void shouldRejectPoBovineWithoutFatherReference() {
        BovineRegistrationData data = registeredData(BovineRegistrationCategory.PO)
                .withMotherRegistrationNumber("EXT-M-001")
                .withoutFatherReference()
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Para bovinos PO ou PC, a referência do pai é obrigatória");
    }

    @Test
    void shouldRejectPoBovineWithoutMotherReference() {
        BovineRegistrationData data = registeredData(BovineRegistrationCategory.PO)
                .withFatherRegistrationNumber("EXT-F-001")
                .withoutMotherReference()
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Para bovinos PO ou PC, a referência da mãe é obrigatória");
    }

    @Test
    void shouldRejectPcBovineWithoutFatherReference() {
        BovineRegistrationData data = registeredData(BovineRegistrationCategory.PC)
                .withMotherId(new BovineId(3L))
                .withoutFatherReference()
                .withoutMotherRegistrationNumber()
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Para bovinos PO ou PC, a referência do pai é obrigatória");
    }

    @Test
    void shouldRejectPcBovineWithoutMotherReference() {
        BovineRegistrationData data = registeredData(BovineRegistrationCategory.PC)
                .withFatherId(new BovineId(2L))
                .withoutFatherRegistrationNumber()
                .withoutMotherReference()
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Para bovinos PO ou PC, a referência da mãe é obrigatória");
    }

    @Test
    void shouldCreateUnregisteredBovineWithoutParentageOrRegistrationDetails() {
        BovineRegistrationData data = registrationData()
                .withCategory(BovineRegistrationCategory.UNREGISTERED)
                .withoutRegistrationDetails()
                .build();

        assertThat(createBovine(data)).isNotNull();
    }

    @Test
    void shouldRejectNullName() {
        BovineRegistrationData data = registrationData()
                .withName(null)
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O nome do bovino é obrigatório");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void shouldRejectBlankName(String name) {
        BovineRegistrationData data = registrationData()
                .withName(name)
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome do bovino não pode estar vazio ou conter somente espaços");
    }

    @Test
    void shouldRejectNullId() {
        assertThatThrownBy(() -> createBovine(null, INTERNAL_CODE, registrationData().build(), CREATED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O identificador do bovino é obrigatório");
    }

    @Test
    void shouldRejectNullInternalCode() {
        assertThatThrownBy(() -> createBovine(BOVINE_ID, null, registrationData().build(), CREATED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O código interno do bovino é obrigatório");
    }

    @Test
    void shouldRejectNullRegistrationData() {
        assertThatThrownBy(() -> createBovine(BOVINE_ID, INTERNAL_CODE, null, CREATED_AT))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Os dados de cadastro do bovino são obrigatórios");
    }

    @Test
    void shouldRejectNullSex() {
        BovineRegistrationData data = registrationData()
                .withSex(null)
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O sexo do bovino é obrigatório");
    }

    @Test
    void shouldRejectNullStatus() {
        BovineRegistrationData data = registrationData()
                .withStatus(null)
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("O status do bovino é obrigatório");
    }

    @Test
    void shouldRejectNullRegistrationCategory() {
        BovineRegistrationData data = registrationData()
                .withCategory(null)
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("A categoria de registro do bovino é obrigatória");
    }

    @Test
    void shouldRejectNullCreatedAt() {
        assertThatThrownBy(() -> createBovine(BOVINE_ID, INTERNAL_CODE, registrationData().build(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("A data de criação é obrigatória");
    }

    @Test
    void shouldRejectFatherIdEqualToBovineId() {
        BovineRegistrationData data = registrationData()
                .withFatherId(BOVINE_ID)
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O identificador do pai não pode ser igual ao do próprio bovino");
    }

    @Test
    void shouldRejectMotherIdEqualToBovineId() {
        BovineRegistrationData data = registrationData()
                .withMotherId(BOVINE_ID)
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O identificador da mãe não pode ser igual ao do próprio bovino");
    }

    @Test
    void shouldRejectBirthDateAfterCreationDateInUtc() {
        BovineRegistrationData data = registrationData()
                .withBirthDate(LocalDate.of(2026, 6, 2))
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data de nascimento não pode ser posterior à data de criação");
    }

    @ParameterizedTest
    @EnumSource(value = BovineRegistrationCategory.class, names = {"PO", "PC", "PA"})
    void shouldRejectRegisteredCategoryWithoutRegistrationNumber(
            BovineRegistrationCategory category
    ) {
        BovineRegistrationData data = registeredData(category)
                .withRegistrationNumber(null)
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O número de registro genealógico é obrigatório para a categoria informada");
    }

    @ParameterizedTest
    @EnumSource(value = BovineRegistrationCategory.class, names = {"PO", "PC", "PA"})
    void shouldRejectRegisteredCategoryWithoutRegistrationAssociation(
            BovineRegistrationCategory category
    ) {
        BovineRegistrationData data = registeredData(category)
                .withRegistrationAssociation(null)
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A associação de registro genealógico é obrigatória para a categoria informada");
    }

    @Test
    void shouldRejectUnregisteredBovineWithOfficialRegistrationDetails() {
        BovineRegistrationData data = registrationData()
                .withCategory(BovineRegistrationCategory.UNREGISTERED)
                .build();

        assertThatThrownBy(() -> createBovine(data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "A categoria sem registro oficial não permite número ou associação genealógica"
                );
    }

    private static Bovine createBovine(BovineRegistrationData data) {
        return createBovine(BOVINE_ID, INTERNAL_CODE, data, CREATED_AT);
    }

    private static Bovine createBovine(
            BovineId id,
            BovineInternalCode internalCode,
            BovineRegistrationData data,
            Instant createdAt
    ) {
        return createBovine(id, internalCode, data, createdAt, BOVINE_ORIGIN);
    }

    private static Bovine createBovine(
            BovineId id,
            BovineInternalCode internalCode,
            BovineRegistrationData data,
            Instant createdAt,
            BovineOrigin origin
    ) {
        return Bovine.createManually(id, internalCode, data, createdAt, origin);
    }

    private static RegistrationDataBuilder registeredData(BovineRegistrationCategory category) {
        RegistrationDataBuilder builder = registrationData()
                .withCategory(category);

        if (category == BovineRegistrationCategory.PO
                || category == BovineRegistrationCategory.PC) {
            builder.withFatherRegistrationNumber("EXT-F-001")
                    .withMotherRegistrationNumber("EXT-M-001");
        }

        return builder;
    }

    private static RegistrationDataBuilder registrationData() {
        return new RegistrationDataBuilder();
    }

    private static final class RegistrationDataBuilder {
        private String name = "Mimosa";
        private BovineSex sex = BovineSex.FEMALE;
        private String breed = "Nelore";
        private String coatColor = "Branca";
        private LocalDate birthDate = LocalDate.of(2024, 1, 15);
        private BovineStatus status = BovineStatus.ACTIVE;
        private String earTag = "TAG-001";
        private String registrationNumber = "REG-2026-001";
        private String registrationAssociation = "ABC";
        private BovineRegistrationCategory category = BovineRegistrationCategory.PA;
        private BovineId fatherId;
        private BovineId motherId;
        private String fatherRegistrationNumber;
        private String motherRegistrationNumber;

        private RegistrationDataBuilder withName(String name) {
            this.name = name;
            return this;
        }

        private RegistrationDataBuilder withSex(BovineSex sex) {
            this.sex = sex;
            return this;
        }

        private RegistrationDataBuilder withStatus(BovineStatus status) {
            this.status = status;
            return this;
        }

        private RegistrationDataBuilder withBirthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        private RegistrationDataBuilder withRegistrationNumber(String registrationNumber) {
            this.registrationNumber = registrationNumber;
            return this;
        }

        private RegistrationDataBuilder withRegistrationAssociation(String registrationAssociation) {
            this.registrationAssociation = registrationAssociation;
            return this;
        }

        private RegistrationDataBuilder withCategory(BovineRegistrationCategory category) {
            this.category = category;
            return this;
        }

        private RegistrationDataBuilder withFatherId(BovineId fatherId) {
            this.fatherId = fatherId;
            return this;
        }

        private RegistrationDataBuilder withMotherId(BovineId motherId) {
            this.motherId = motherId;
            return this;
        }

        private RegistrationDataBuilder withFatherRegistrationNumber(String registrationNumber) {
            this.fatherRegistrationNumber = registrationNumber;
            return this;
        }

        private RegistrationDataBuilder withMotherRegistrationNumber(String registrationNumber) {
            this.motherRegistrationNumber = registrationNumber;
            return this;
        }

        private RegistrationDataBuilder withoutRegistrationDetails() {
            this.registrationNumber = null;
            this.registrationAssociation = null;
            return this;
        }

        private RegistrationDataBuilder withoutFatherReference() {
            this.fatherId = null;
            this.fatherRegistrationNumber = null;
            return this;
        }

        private RegistrationDataBuilder withoutMotherReference() {
            this.motherId = null;
            this.motherRegistrationNumber = null;
            return this;
        }

        private RegistrationDataBuilder withoutFatherRegistrationNumber() {
            this.fatherRegistrationNumber = null;
            return this;
        }

        private RegistrationDataBuilder withoutMotherRegistrationNumber() {
            this.motherRegistrationNumber = null;
            return this;
        }

        private BovineRegistrationData build() {
            return new BovineRegistrationData(
                    name,
                    sex,
                    breed,
                    coatColor,
                    birthDate,
                    status,
                    earTag,
                    registrationNumber,
                    registrationAssociation,
                    category,
                    fatherId,
                    motherId,
                    fatherRegistrationNumber,
                    motherRegistrationNumber
            );
        }
    }
}
