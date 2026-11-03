package com.example.trabalho1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para avaliar um jogo. O autor é o usuário autenticado (vem do token, não do corpo)")
public record AvaliacaoRequestDTO(

        @Schema(description = "Identificador do jogo avaliado", example = "1")
        @NotNull(message = "ID do jogo é obrigatório")
        Long jogoId,

        @Schema(description = "Nota de 1 a 5", example = "5", minimum = "1", maximum = "5")
        @NotNull(message = "Nota é obrigatória")
        @Min(value = 1, message = "Nota deve ser no mínimo 1")
        @Max(value = 5, message = "Nota deve ser no máximo 5")
        Integer nota,

        @Schema(description = "Comentário opcional", example = "Obra-prima!")
        @Size(max = 2000, message = "Comentário deve ter no máximo 2000 caracteres")
        String comentario
) {
}
