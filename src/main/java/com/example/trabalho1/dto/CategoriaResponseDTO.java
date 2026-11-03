package com.example.trabalho1.dto;

import com.example.trabalho1.entity.Categoria;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoria (gênero) de jogos")
public record CategoriaResponseDTO(
        @Schema(description = "Identificador da categoria", example = "1")
        Long id,

        @Schema(example = "RPG")
        String nome,

        @Schema(example = "Jogos de interpretação de papéis")
        String descricao
) {

    public static CategoriaResponseDTO fromEntity(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao()
        );
    }
}
