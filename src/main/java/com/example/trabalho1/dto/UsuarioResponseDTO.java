package com.example.trabalho1.dto;

import java.time.LocalDateTime;

import com.example.trabalho1.entity.Role;
import com.example.trabalho1.entity.Usuario;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        Role role,
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
