package com.example.trabalho1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DesenvolvedoraRequestDTO(

        @Schema(example = "Valve Corporation")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,

        @Schema(example = "Estados Unidos")
        @Size(max = 100, message = "País de origem deve ter no máximo 100 caracteres")
        String paisOrigem,

        @Schema(example = "https://www.valvesoftware.com")
        @Size(max = 255, message = "Site deve ter no máximo 255 caracteres")
        @Pattern(regexp = "^(https?://).+$", message = "Site deve começar com http:// ou https://")
        String site
) {
}
