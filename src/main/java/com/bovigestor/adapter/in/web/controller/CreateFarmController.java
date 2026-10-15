package com.bovigestor.adapter.in.web.controller;

import com.bovigestor.adapter.in.web.dto.CreateFarmResponse;
import com.bovigestor.adapter.in.web.dto.FarmRequest;
import com.bovigestor.adapter.in.web.mapper.FarmWebMapper;
import com.bovigestor.application.port.in.CreateFarmUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/farms")
@Tag(name = "Fazendas", description = "Operações de cadastro e consulta de fazendas.")
public class CreateFarmController {

    private final CreateFarmUseCase useCase;

    public CreateFarmController(CreateFarmUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @Operation(
            operationId = "createFarm",
            summary = "Cadastrar uma fazenda",
            description = "Valida os dados recebidos e cadastra uma fazenda. "
                    + "A resposta contém somente o identificador criado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Fazenda cadastrada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateFarmResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo ausente, JSON inválido ou campos fora das regras de validação.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A solicitação conflita com dados já existentes.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "405",
                    description = "O método HTTP solicitado não é suportado para este recurso.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "415",
                    description = "O tipo de conteúdo da requisição não é suportado.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falha inesperada ao processar a solicitação.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<CreateFarmResponse> create(@Valid @RequestBody FarmRequest request) {

        var command = FarmWebMapper.toCommand(request);
        var farmId = useCase.create(command);
        var response = new CreateFarmResponse(farmId.value());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
