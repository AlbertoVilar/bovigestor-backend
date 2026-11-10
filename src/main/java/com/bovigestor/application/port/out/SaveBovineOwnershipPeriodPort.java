package com.bovigestor.application.port.out;

import com.bovigestor.domain.model.BovineOwnershipPeriod;

public interface SaveBovineOwnershipPeriodPort {

    void save(BovineOwnershipPeriod period);
}
