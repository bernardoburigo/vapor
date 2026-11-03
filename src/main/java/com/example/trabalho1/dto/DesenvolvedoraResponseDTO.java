package com.example.trabalho1.dto;

import com.example.trabalho1.entity.Desenvolvedora;

public record DesenvolvedoraResponseDTO(
        Long id,
        String nome,
        String paisOrigem,
        String site
) {

    public static DesenvolvedoraResponseDTO fromEntity(Desenvolvedora desenvolvedora) {
        return new DesenvolvedoraResponseDTO(
                desenvolvedora.getId(),
                desenvolvedora.getNome(),
                desenvolvedora.getPaisOrigem(),
                desenvolvedora.getSite()
        );
    }
}
