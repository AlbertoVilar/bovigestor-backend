package com.bovigestor.domain.model;

import java.time.Instant;
import java.util.Objects;

public class Farm {

    private final FarmId id;
    private String name;
    private String codeSeries;
    private FarmStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Farm(
            FarmId id,
            String name,
            String codeSeries,
            FarmStatus status,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "O identificador da fazenda é obrigatório");
        this.name = validateName(name);
        this.codeSeries = validateCodeSeries(codeSeries);
        this.status = Objects.requireNonNull(status, "O status da fazenda é obrigatório");
        this.createdAt = Objects.requireNonNull(createdAt, "A data de criação é obrigatória");
        this.updatedAt = createdAt;
    }

    public static Farm create(
            FarmId id,
            String name,
            String codeSeries,
            Instant createdAt
    ) {
        return new Farm(id, name, codeSeries, FarmStatus.ACTIVE, createdAt);
    }

    public void activate(Instant occurredAt) {
        if (status == FarmStatus.ACTIVE) {
            return;
        }

        changeStatus(FarmStatus.ACTIVE, occurredAt);
    }

    public void deactivate(Instant occurredAt) {
        if (status == FarmStatus.INACTIVE) {
            return;
        }

        changeStatus(FarmStatus.INACTIVE, occurredAt);
    }

    public FarmId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String codeSeries() {
        return codeSeries;
    }

    public FarmStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private void changeStatus(FarmStatus newStatus, Instant occurredAt) {
        Objects.requireNonNull(occurredAt, "A data da alteração é obrigatória");

        if (occurredAt.isBefore(createdAt) || occurredAt.isBefore(updatedAt)) {
            throw new IllegalArgumentException(
                    "A data da alteração não pode ser anterior à criação ou à última atualização"
            );
        }

        status = newStatus;
        updatedAt = occurredAt;
    }

    private static String validateName(String name) {
        Objects.requireNonNull(name, "O nome da fazenda é obrigatório");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da fazenda não pode estar vazio ou conter somente espaços"
            );
        }

        return name;
    }

    private static String validateCodeSeries(String codeSeries) {
        Objects.requireNonNull(codeSeries, "A série da fazenda é obrigatória");

        if (!codeSeries.matches("[A-Z]{3,4}")) {
            throw new IllegalArgumentException(
                    "A série da fazenda deve conter três ou quatro letras maiúsculas"
            );
        }

        return codeSeries;
    }
}
