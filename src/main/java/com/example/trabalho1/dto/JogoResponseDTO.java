package com.example.trabalho1.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import com.example.trabalho1.entity.Desenvolvedora;
import com.example.trabalho1.entity.Jogo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Jogo do catálogo, com a nota média calculada a partir das avaliações")
public record JogoResponseDTO(
        @Schema(description = "Identificador do jogo", example = "1")
        Long id,

        @Schema(example = "Half-Life 3")
        String titulo,

        @Schema(example = "O aguardado retorno de Gordon Freeman")
        String descricao,

        @Schema(example = "199.90")
        BigDecimal preco,

        @Schema(example = "2026-11-15")
        LocalDate dataLancamento,

        DesenvolvedoraResumo desenvolvedora,

        List<CategoriaResponseDTO> categorias,

        @Schema(description = "Média das notas (1 a 5, uma casa decimal); nulo se o jogo ainda não foi avaliado",
                example = "4.5", nullable = true)
        Double notaMedia,

        @Schema(description = "Quantidade de avaliações recebidas", example = "12")
        long totalAvaliacoes
) {

    @Schema(description = "Dados resumidos da desenvolvedora do jogo")
    public record DesenvolvedoraResumo(
            @Schema(example = "1") Long id,
            @Schema(example = "Valve Corporation") String nome
    ) {
    }

    public static JogoResponseDTO fromEntity(Jogo jogo, Double notaMedia, long totalAvaliacoes) {
        Desenvolvedora desenvolvedora = jogo.getDesenvolvedora();
        return new JogoResponseDTO(
                jogo.getId(),
                jogo.getTitulo(),
                jogo.getDescricao(),
                jogo.getPreco(),
                jogo.getDataLancamento(),
                new DesenvolvedoraResumo(desenvolvedora.getId(), desenvolvedora.getNome()),
                jogo.getCategorias().stream()
                        .sorted(Comparator.comparing(c -> c.getNome().toLowerCase()))
                        .map(CategoriaResponseDTO::fromEntity)
                        .toList(),
                notaMedia,
                totalAvaliacoes
        );
    }
}
