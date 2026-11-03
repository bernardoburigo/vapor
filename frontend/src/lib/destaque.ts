import type { Jogo } from '../api/types'

/** Abaixo disso, uma ou duas notas altas não dizem muito sobre o jogo. */
export const MINIMO_AVALIACOES = 3

/**
 * O jogo de maior nota média; em caso de empate, o que tem mais avaliações. Dá preferência a jogos com ao
 * menos {@link MINIMO_AVALIACOES} avaliações e, se nenhum chegar a isso, considera todos os avaliados.
 * Jogos sem avaliações nunca são escolhidos.
 */
export function escolherDestaque(jogos: Jogo[]): Jogo | null {
  const avaliados = jogos.filter((jogo) => jogo.notaMedia != null)
  const confiaveis = avaliados.filter((jogo) => jogo.totalAvaliacoes >= MINIMO_AVALIACOES)
  return melhorDe(confiaveis.length > 0 ? confiaveis : avaliados)
}

function melhorDe(jogos: Jogo[]): Jogo | null {
  let melhor: Jogo | null = null
  for (const jogo of jogos) {
    if (
      melhor === null ||
      jogo.notaMedia! > melhor.notaMedia! ||
      (jogo.notaMedia === melhor.notaMedia && jogo.totalAvaliacoes > melhor.totalAvaliacoes)
    ) {
      melhor = jogo
    }
  }
  return melhor
}
