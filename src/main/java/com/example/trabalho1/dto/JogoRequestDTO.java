package com.example.trabalho1.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou atualização de um jogo")
public record JogoRequestDTO(

        @Schema(description = "Título do jogo", example = "Half-Life 3")
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
        String titulo,

        @Schema(description = "Descrição opcional do jogo", example = "O aguardado retorno de Gordon Freeman")
        String descricao,

        @Schema(description = "Preço em reais (0 para jogos gratuitos)", example = "199.90")
        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.00", message = "Preço não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "Preço deve ter no máximo 8 dígitos inteiros e 2 decimais")
        BigDecimal preco,

        @Schema(description = "Data de lançamento (ISO 8601)", example = "2026-11-15")
        LocalDate dataLancamento,

        @Schema(description = "Identificador da desenvolvedora que publica o jogo", example = "1")
        @NotNull(message = "ID da desenvolvedora é obrigatório")
        Long desenvolvedoraId,

        @Schema(description = "Identificadores das categorias do jogo (ao menos uma)", example = "[1, 2]")
        @NotEmpty(message = "Informe ao menos uma categoria")
        Set<@NotNull(message = "ID de categoria não pode ser nulo") Long> categoriaIds
) {
}
