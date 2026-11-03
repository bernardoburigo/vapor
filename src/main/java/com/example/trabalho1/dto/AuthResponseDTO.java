package com.example.trabalho1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta do login: token JWT e dados do usuário autenticado")
public record AuthResponseDTO(
        @Schema(description = "Token JWT; envie no header Authorization como 'Bearer <token>'",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJiZXJuYXJkb0B2YXBvci5jb20ifQ.assinatura")
        String token,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tipo,

        @Schema(description = "Validade do token, em segundos", example = "3600")
        long expiraEmSegundos,

        UsuarioResponseDTO usuario
) {

    public static AuthResponseDTO bearer(String token, long expiraEmSegundos, UsuarioResponseDTO usuario) {
        return new AuthResponseDTO(token, "Bearer", expiraEmSegundos, usuario);
    }
}
