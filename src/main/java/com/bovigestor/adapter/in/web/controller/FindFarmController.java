package com.bovigestor.adapter.in.web.controller;

import com.bovigestor.adapter.in.web.dto.FarmResponse;
import com.bovigestor.adapter.in.web.mapper.FarmWebMapper;
import com.bovigestor.application.port.in.FindFarmByIdUseCase;
import com.bovigestor.domain.model.FarmId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/farms")
@Tag(name = "Fazendas", description = "Operações de cadastro e consulta de fazendas.")
public class FindFarmController {

    private final FindFarmByIdUseCase findFarmByIdUseCase;

    public FindFarmController(FindFarmByIdUseCase findFarmByIdUseCase) {
        this.findFarmByIdUseCase = findFarmByIdUseCase;
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "findFarmById",
            summary = "Consultar uma fazenda pelo identificador"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Fazenda encontrada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FarmResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "O identificador informado é inválido.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Fazenda não encontrada.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<FarmResponse> findById(@PathVariable Long id) {
        var farm = findFarmByIdUseCase.findById(new FarmId(id));
        return ResponseEntity.ok(FarmWebMapper.toResponse(farm));
    }
}
