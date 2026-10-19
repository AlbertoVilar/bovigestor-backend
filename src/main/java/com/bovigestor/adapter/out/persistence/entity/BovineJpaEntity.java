package com.bovigestor.adapter.out.persistence.entity;

import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "bovine")
public class BovineJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "internal_code", nullable = false, length = 255)
    private String internalCode;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex", nullable = false, length = 20)
    private BovineSex sex;

    @Column(name = "breed", length = 255)
    private String breed;

    @Column(name = "coat_color", length = 255)
    private String coatColor;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BovineStatus status;

    @Column(name = "ear_tag", length = 255)
    private String earTag;

    @Column(name = "registration_number", length = 255)
    private String registrationNumber;

    @Column(name = "registration_association", length = 255)
    private String registrationAssociation;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_category", nullable = false, length = 20)
    private BovineRegistrationCategory registrationCategory;

    @Column(name = "father_id")
    private Long fatherId;

    @Column(name = "mother_id")
    private Long motherId;

    @Column(name = "father_registration_number", length = 255)
    private String fatherRegistrationNumber;

    @Column(name = "mother_registration_number", length = 255)
    private String motherRegistrationNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BovineJpaEntity() {
    }

    public BovineJpaEntity(
            Long id,
            String internalCode,
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
            Long fatherId,
            Long motherId,
            String fatherRegistrationNumber,
            String motherRegistrationNumber,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.internalCode = internalCode;
        this.name = name;
        this.sex = sex;
        this.breed = breed;
        this.coatColor = coatColor;
        this.birthDate = birthDate;
        this.status = status;
        this.earTag = earTag;
        this.registrationNumber = registrationNumber;
        this.registrationAssociation = registrationAssociation;
        this.registrationCategory = registrationCategory;
        this.fatherId = fatherId;
        this.motherId = motherId;
        this.fatherRegistrationNumber = fatherRegistrationNumber;
        this.motherRegistrationNumber = motherRegistrationNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getInternalCode() {
        return internalCode;
    }

    public String getName() {
        return name;
    }

    public BovineSex getSex() {
        return sex;
    }

    public String getBreed() {
        return breed;
    }

    public String getCoatColor() {
        return coatColor;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public BovineStatus getStatus() {
        return status;
    }

    public String getEarTag() {
        return earTag;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getRegistrationAssociation() {
        return registrationAssociation;
    }

    public BovineRegistrationCategory getRegistrationCategory() {
        return registrationCategory;
    }

    public Long getFatherId() {
        return fatherId;
    }

    public Long getMotherId() {
        return motherId;
    }

    public String getFatherRegistrationNumber() {
        return fatherRegistrationNumber;
    }

    public String getMotherRegistrationNumber() {
        return motherRegistrationNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
