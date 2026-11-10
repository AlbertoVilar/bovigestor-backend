package com.bovigestor.application.port.in;

import com.bovigestor.domain.model.Bovine;
import com.bovigestor.domain.model.BovineId;

public interface FindBovineByIdUseCase {
    Bovine findById(BovineId bovineId);
}
