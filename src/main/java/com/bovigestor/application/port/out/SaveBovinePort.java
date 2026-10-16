package com.bovigestor.application.port.out;

import com.bovigestor.domain.model.Bovine;

public interface SaveBovinePort {

    void save(Bovine bovine);
}
