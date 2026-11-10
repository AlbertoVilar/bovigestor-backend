package com.bovigestor.application.port.out;

import com.bovigestor.domain.model.BovineOwnershipPeriodId;

public interface BovineOwnershipPeriodIdGenerationPort {
    BovineOwnershipPeriodId nextId();
}
