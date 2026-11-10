package com.bovigestor.application.port.in;

import com.bovigestor.domain.model.Farm;
import com.bovigestor.domain.model.FarmId;

public interface FindFarmByIdUseCase {
    Farm findById(FarmId farmId);
}
