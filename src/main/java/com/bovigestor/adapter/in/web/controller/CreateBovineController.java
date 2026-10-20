package com.bovigestor.adapter.in.web.controller;

import com.bovigestor.adapter.in.web.dto.BovineRequest;
import com.bovigestor.adapter.in.web.dto.CreateBovineResponse;
import com.bovigestor.adapter.in.web.mapper.BovineWebMapper;
import com.bovigestor.application.port.in.CreateBovineUseCase;
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
@ConditionalOnProperty(
        prefix = "bovigestor.bovine.creation",
        name = "enabled",
        havingValue = "true"
)
@Tag(name = "Bovinos", description = "Cadastro técnico de bovinos no ambiente de desenvolvimento.")
public class CreateBovineController {

    private final CreateBovineUseCase useCase;

    public CreateBovineController(CreateBovineUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @Operation(
            operationId = "createBovine",
            summary = "Cadastrar tecnicamente um bovino (somente desenvolvimento)",
            description = "Cria um bovino e persiste seus dados cadastrais. Este endpoint é habilitado "
                    + "exclusivamente no perfil dev e não cria titularidade inicial, não representa "
                    + "cadastro operacional completo e não deve ser exposto em produção."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Bovino cadastrado tecnicamente. A resposta contém somente o ID criado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateBovineResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "JSON inválido, campos inválidos ou referência genealógica local inexistente.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "O código interno já está em uso globalmente.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falha inesperada sem exposição de detalhes internos.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<CreateBovineResponse> create(@Valid @RequestBody BovineRequest request) {
        var bovineId = useCase.create(BovineWebMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateBovineResponse(bovineId.value()));
    }
}
