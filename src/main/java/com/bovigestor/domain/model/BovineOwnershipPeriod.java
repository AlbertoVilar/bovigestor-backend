package com.bovigestor.domain.model;

import java.time.Instant;
import java.util.Objects;

public class BovineOwnershipPeriod {
    private final BovineOwnershipPeriodId id;
    private final BovineId bovineId;
    private final FarmId farmId;
    private final Instant startedAt;
    private final Instant endedAt;

    private BovineOwnershipPeriod(
            BovineOwnershipPeriodId id,
            BovineId bovineId,
            FarmId farmId,
            Instant startedAt,
            Instant endedAt
    ) {
        this.id = Objects.requireNonNull(
                id,
                "O identificador do período é obrigatório"
        );
        this.bovineId = Objects.requireNonNull(
                bovineId,
                "O identificador do bovino é obrigatório"
        );
        this.farmId = Objects.requireNonNull(
                farmId,
                "A fazenda do período é obrigatória"
        );
        this.startedAt = Objects.requireNonNull(
                startedAt,
                "O início do período é obrigatório"
        );

        if (endedAt != null && !endedAt.isAfter(startedAt)) {
            throw new IllegalArgumentException(
                    "O fim do período deve ser posterior ao início"
            );
        }
        this.endedAt = endedAt;
    }

    public static BovineOwnershipPeriod open(
            BovineOwnershipPeriodId id,
            BovineId bovineId,
            FarmId farmId,
            Instant startedAt
    ) {
        return new BovineOwnershipPeriod(
                id, bovineId, farmId, startedAt, null
        );
    }

    public boolean contains(Instant instant) {
        Objects.requireNonNull(instant, "O instante consultado é obrigatório");
        return !instant.isBefore(startedAt)
                && (endedAt == null || instant.isBefore(endedAt));
    }

    public BovineOwnershipPeriod closeAt(Instant at) {

        Objects.requireNonNull(at, "O instante de encerramento é obrigatório");

        if (endedAt != null) {
            throw new IllegalStateException(
                    "O período já está encerrado"
            );
        }
        return new BovineOwnershipPeriod(
                id,
                bovineId,
                farmId,
                startedAt,
                at
        );
    }

    public BovineId bovineId() {
        return bovineId;
    }

    public BovineOwnershipPeriodId id() {
        return id;
    }

    public FarmId farmId() {
        return farmId;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant endedAt() {
        return endedAt;
    }
}
