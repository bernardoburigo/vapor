package com.example.trabalho1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoRequestDTO(

        @NotNull(message = "ID do usuário é obrigatório")
        Long usuarioId,

        @NotNull(message = "ID do jogo é obrigatório")
        Long jogoId,

        @NotNull(message = "Nota é obrigatória")
        @Min(value = 1, message = "Nota deve ser no mínimo 1")
        @Max(value = 5, message = "Nota deve ser no máximo 5")
        Integer nota,

        @Size(max = 2000, message = "Comentário deve ter no máximo 2000 caracteres")
        String comentario
) {
}
