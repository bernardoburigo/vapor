package com.example.trabalho1.dto;

import java.time.LocalDateTime;

import com.example.trabalho1.entity.Role;
import com.example.trabalho1.entity.Usuario;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados públicos de um usuário (a senha nunca é retornada)")
public record UsuarioResponseDTO(
        @Schema(description = "Identificador do usuário", example = "1")
        Long id,

        @Schema(example = "Bernardo Búrigo")
        String nome,

        @Schema(example = "bernardo@vapor.com")
        String email,

        @Schema(description = "Perfil de acesso", example = "USER")
        Role role,

        @Schema(description = "Data e hora do cadastro", example = "2026-10-06T14:30:00")
        LocalDateTime dataCadastro
) {

    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getDataCadastro()
        );
    }
}
