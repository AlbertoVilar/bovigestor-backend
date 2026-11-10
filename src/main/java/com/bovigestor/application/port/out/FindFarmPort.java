package com.bovigestor.application.port.out;

import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;

import java.util.Optional;

public interface FindFarmPort {
    Optional<Farm> findById(FarmId farmId);
}
