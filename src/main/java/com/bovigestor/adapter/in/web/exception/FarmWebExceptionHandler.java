package com.bovigestor.adapter.in.web.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
public class FarmWebExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FarmWebExceptionHandler.class);
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";
    private static final String FOREIGN_KEY_VIOLATION_SQL_STATE = "23503";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() == null
                                ? "Valor inválido"
                                : error.getDefaultMessage(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos informados são inválidos."
        );
        problem.setTitle("Requisição inválida");
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableBody(
            HttpMessageNotReadableException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisição está ausente ou possui formato inválido."
        );
        problem.setTitle("Corpo da requisição inválido");

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.METHOD_NOT_ALLOWED,
                "O método HTTP solicitado não é suportado para este recurso."
        );
        problem.setTitle("Método não permitido");

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(problem);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "O tipo de conteúdo da requisição não é suportado."
        );
        problem.setTitle("Tipo de conteúdo não suportado");

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(problem);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(NoResourceFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "O recurso solicitado não foi encontrado."
        );
        problem.setTitle("Recurso não encontrado");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        if (hasSqlState(exception, UNIQUE_VIOLATION_SQL_STATE)) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.CONFLICT,
                    "A solicitação conflita com dados já existentes."
            );
            problem.setTitle("Conflito de dados");

            return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
        }

        if (hasSqlState(exception, FOREIGN_KEY_VIOLATION_SQL_STATE)) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    "Uma referência local informada não existe."
            );
            problem.setTitle("Referência inválida");

            return ResponseEntity.badRequest().body(problem);
        }

        return internalServerError(exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDomainInput(IllegalArgumentException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais valores informados não atendem às regras de cadastro."
        );
        problem.setTitle("Dados inválidos");

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception exception) {
        return internalServerError(exception);
    }

    private static boolean hasSqlState(Throwable exception, String expectedSqlState) {
        Deque<Throwable> pending = new ArrayDeque<>();
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        pending.add(exception);

        while (!pending.isEmpty()) {
            Throwable current = pending.removeFirst();
            if (!visited.add(current)) {
                continue;
            }

            if (current instanceof SQLException sqlException) {
                if (expectedSqlState.equals(sqlException.getSQLState())) {
                    return true;
                }

                for (SQLException next = sqlException.getNextException();
                     next != null && visited.add(next);
                     next = next.getNextException()) {
                    if (expectedSqlState.equals(next.getSQLState())) {
                        return true;
                    }
                }
            }

            if (current.getCause() != null) {
                pending.addLast(current.getCause());
            }
        }

        return false;
    }

    private ResponseEntity<ProblemDetail> internalServerError(Exception exception) {
        LOGGER.error("Unexpected error while processing a web request", exception);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Não foi possível processar a solicitação."
        );
        problem.setTitle("Erro interno do servidor");

        return ResponseEntity.internalServerError().body(problem);
    }
}
