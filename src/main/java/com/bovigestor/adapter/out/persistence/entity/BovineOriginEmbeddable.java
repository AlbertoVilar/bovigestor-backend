package com.bovigestor.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class BovineOriginEmbeddable {

    @Column(name = "origin_creator_name", nullable = false, length = 255)
    private String creatorName;

    @Column(name = "origin_farm_name", nullable = false, length = 255)
    private String originFarmName;

    @Column(name = "origin_farm_id")
    private Long originFarmId;

    protected BovineOriginEmbeddable() {
    }

    public BovineOriginEmbeddable(String creatorName, String originFarmName, Long originFarmId) {
        this.creatorName = requireNonBlank(creatorName, "O nome do criador é obrigatório");
        this.originFarmName = requireNonBlank(
                originFarmName,
                "O nome da fazenda de origem é obrigatório"
        );
        this.originFarmId = originFarmId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getOriginFarmName() {
        return originFarmName;
    }

    public Long getOriginFarmId() {
        return originFarmId;
    }

    private static String requireNonBlank(String value, String message) {
        Objects.requireNonNull(value, message);
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        if (value.length() > 255) {
            throw new IllegalArgumentException("O nome da origem deve conter no máximo 255 caracteres");
        }
        return value;
    }
}
