package com.example.trabalho1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou atualização de uma categoria (gênero) de jogos")
public record CategoriaRequestDTO(

        @Schema(description = "Nome único da categoria", example = "RPG")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 80, message = "Nome deve ter no máximo 80 caracteres")
        String nome,

        @Schema(description = "Descrição opcional da categoria", example = "Jogos de interpretação de papéis")
        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String descricao
) {
}
