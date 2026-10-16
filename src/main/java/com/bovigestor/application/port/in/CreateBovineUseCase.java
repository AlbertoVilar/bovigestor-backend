package com.bovigestor.application.port.in;

import com.bovigestor.domain.model.BovineId;

public interface CreateBovineUseCase {

    BovineId create(CreateBovineCommand command);
}
