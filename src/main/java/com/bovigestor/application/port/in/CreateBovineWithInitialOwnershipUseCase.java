package com.bovigestor.application.port.in;

import com.bovigestor.domain.model.BovineId;

public interface CreateBovineWithInitialOwnershipUseCase {

    BovineId create(
            CreateBovineWithInitialOwnershipCommand command
    );
}
