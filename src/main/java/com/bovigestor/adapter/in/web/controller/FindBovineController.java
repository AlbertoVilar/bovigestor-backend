package com.bovigestor.adapter.in.web.controller;

import com.bovigestor.adapter.in.web.dto.BovineResponse;
import com.bovigestor.adapter.in.web.mapper.BovineWebMapper;
import com.bovigestor.application.port.in.FindBovineByIdUseCase;
import com.bovigestor.domain.model.BovineId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bovines")
public class FindBovineController {

    private final FindBovineByIdUseCase findBovineByIdUseCase;

    public FindBovineController(FindBovineByIdUseCase findBovineByIdUseCase) {
        this.findBovineByIdUseCase = findBovineByIdUseCase;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BovineResponse> findById(@PathVariable Long id) {
        var bovine = findBovineByIdUseCase.findById(new BovineId(id));
        return ResponseEntity.ok(BovineWebMapper.toResponse(bovine));
    }

}
