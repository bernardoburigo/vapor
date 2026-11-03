package com.example.trabalho1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para alterar uma avaliação existente")
public record AvaliacaoUpdateRequestDTO(

        @Schema(description = "Nova nota, de 1 a 5", example = "4", minimum = "1", maximum = "5")
        @NotNull(message = "Nota é obrigatória")
        @Min(value = 1, message = "Nota deve ser no mínimo 1")
        @Max(value = 5, message = "Nota deve ser no máximo 5")
        Integer nota,

        @Schema(description = "Novo comentário (opcional)", example = "Melhorou depois do patch")
        @Size(max = 2000, message = "Comentário deve ter no máximo 2000 caracteres")
        String comentario
) {
}
