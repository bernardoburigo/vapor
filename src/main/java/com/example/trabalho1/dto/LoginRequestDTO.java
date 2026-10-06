package com.example.trabalho1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para autenticação")
public record LoginRequestDTO(

        @Schema(description = "Email cadastrado", example = "bernardo@vapor.com")
        @NotBlank(message = "Email é obrigatório")
        String email,

        @Schema(description = "Senha do usuário", example = "senha123", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "Senha é obrigatória")
        String senha
) {
}
