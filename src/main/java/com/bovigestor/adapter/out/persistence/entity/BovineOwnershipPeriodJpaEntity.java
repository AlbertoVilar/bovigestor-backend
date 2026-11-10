package com.bovigestor.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "bovine_ownership_period")
public class BovineOwnershipPeriodJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "bovine_id", nullable = false)
    private Long bovineId;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    protected BovineOwnershipPeriodJpaEntity() {
    }

    public BovineOwnershipPeriodJpaEntity(
            Long id,
            Long bovineId,
            Long farmId,
            Instant startedAt,
            Instant endedAt
    ) {
        this.id = id;
        this.bovineId = bovineId;
        this.farmId = farmId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getBovineId() {
        return bovineId;
    }

    public Long getFarmId() {
        return farmId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }
}
