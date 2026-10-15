package com.bovigestor.application.port.out;

import com.bovigestor.domain.model.FarmId;

public interface FarmIdGenerationPort {

    FarmId nextId();
}
