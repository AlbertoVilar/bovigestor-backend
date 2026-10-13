package com.bovigestor.application.port.out;

import com.bovigestor.domain.model.BovineId;

public interface BovineIdGenerationPort {

    BovineId nextId();
}
