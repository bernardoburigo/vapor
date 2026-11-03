package com.example.trabalho1.dto;

import java.time.LocalDateTime;

import com.example.trabalho1.entity.Avaliacao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Avaliação de um jogo feita por um usuário")
public record AvaliacaoResponseDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long usuarioId,
        @Schema(example = "João Vitor") String usuarioNome,
        @Schema(example = "1") Long jogoId,
        @Schema(example = "Half-Life 3") String jogoTitulo,
        @Schema(example = "5") Integer nota,
        @Schema(example = "Obra-prima!") String comentario,
        @Schema(example = "2026-10-06T14:30:00") LocalDateTime dataAvaliacao
) {

    public static AvaliacaoResponseDTO fromEntity(Avaliacao avaliacao) {
        return new AvaliacaoResponseDTO(
                avaliacao.getId(),
                avaliacao.getUsuario().getId(),
                avaliacao.getUsuario().getNome(),
                avaliacao.getJogo().getId(),
                avaliacao.getJogo().getTitulo(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getDataAvaliacao()
        );
    }
}
