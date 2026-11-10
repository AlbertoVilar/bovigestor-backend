package com.bovigestor.adapter.in.web.controller;

import com.bovigestor.adapter.in.web.dto.BovineRequest;
import com.bovigestor.adapter.in.web.dto.CreateBovineResponse;
import com.bovigestor.adapter.in.web.mapper.BovineWebMapper;
import com.bovigestor.application.port.in.CreateBovineWithInitialOwnershipUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bovines")
@Profile("dev & !prod")
@ConditionalOnProperty(prefix = "bovigestor.bovine.creation", name = "enabled", havingValue = "true")
@Tag(name = "Bovinos", description = "Cadastro operacional de bovinos no ambiente de desenvolvimento.")
public class CreateBovineController {
    private final CreateBovineWithInitialOwnershipUseCase useCase;

    public CreateBovineController(CreateBovineWithInitialOwnershipUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @Operation(
            operationId = "createBovine",
            summary = "Cadastrar bovino com custódia inicial (somente desenvolvimento)",
            description = "Cria o bovino e seu primeiro período de custódia na fazenda informada, "
                    + "em uma única transação. Disponível apenas no perfil dev com a função habilitada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bovino e custódia inicial cadastrados.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreateBovineResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de cadastro inválidos.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Fazenda de ingresso inexistente.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Fazenda inativa ou código interno já utilizado.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Falha inesperada sem detalhes internos.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CreateBovineResponse> create(@Valid @RequestBody BovineRequest request) {
        var id = useCase.create(BovineWebMapper.toOperationalCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateBovineResponse(id.value()));
    }
}
