package com.example.trabalho1.exception;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Formato padrão de erro retornado pela API")
public record ErrorResponse(
        @Schema(description = "Momento em que o erro ocorreu", example = "2026-10-06T14:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Código HTTP do erro", example = "400")
        int status,

        @Schema(description = "Descrição curta do status HTTP", example = "Bad Request")
        String error,

        @Schema(description = "Mensagem explicando o erro", example = "Dados inválidos")
        String message,

        @Schema(description = "Erros por campo, presentes apenas em falhas de validação",
                example = "{\"email\": \"Email inválido\", \"senha\": \"Senha deve ter entre 6 e 100 caracteres\"}")
        Map<String, String> fieldErrors
) {

    public ErrorResponse(int status, String error, String message) {
        this(LocalDateTime.now(), status, error, message, null);
    }

    public ErrorResponse(int status, String error, String message, Map<String, String> fieldErrors) {
        this(LocalDateTime.now(), status, error, message, fieldErrors);
    }
}
