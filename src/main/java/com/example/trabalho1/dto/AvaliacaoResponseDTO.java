package com.example.trabalho1.dto;

import java.time.LocalDateTime;

import com.example.trabalho1.entity.Avaliacao;

public record AvaliacaoResponseDTO(
        Long id,
        Long usuarioId,
        String usuarioNome,
        Long jogoId,
        String jogoTitulo,
        Integer nota,
        String comentario,
        LocalDateTime dataAvaliacao
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
