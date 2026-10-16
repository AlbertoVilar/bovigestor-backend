package com.bovigestor.application.port.in;

import com.bovigestor.domain.model.FarmId;

public interface CreateFarmUseCase {

    FarmId create(CreateFarmCommand command);
}
