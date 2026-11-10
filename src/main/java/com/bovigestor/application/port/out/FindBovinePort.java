package com.bovigestor.application.port.out;

import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;

import java.util.Optional;

public interface FindBovinePort {
    Optional<Bovine> findById(BovineId bovineId);
}
