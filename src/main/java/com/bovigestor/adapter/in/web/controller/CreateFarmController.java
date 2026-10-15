package com.bovigestor.adapter.in.web.controller;

import com.bovigestor.adapter.in.web.dto.CreateFarmResponse;
import com.bovigestor.adapter.in.web.dto.FarmRequest;
import com.bovigestor.adapter.in.web.mapper.FarmWebMapper;
import com.bovigestor.application.port.in.CreateFarmUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/farms")
public class CreateFarmController {

    private final CreateFarmUseCase useCase;

    public CreateFarmController(CreateFarmUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<CreateFarmResponse> create(@RequestBody FarmRequest request) {

        var command = FarmWebMapper.toCommand(request);
        var farmId = useCase.create(command);
        var response = new CreateFarmResponse(farmId.value());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
