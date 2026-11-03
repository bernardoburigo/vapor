import { describe, expect, it } from 'vitest'
import type { Jogo } from '../api/types'
import { escolherDestaque } from './destaque'

function jogo(id: number, notaMedia: number | null, totalAvaliacoes: number): Jogo {
  return {
    id,
    titulo: `Jogo ${id}`,
    descricao: null,
    preco: 10,
    dataLancamento: null,
    desenvolvedora: { id: 1, nome: 'Dev' },
    categorias: [],
    notaMedia,
    totalAvaliacoes,
  }
}

describe('escolherDestaque', () => {
  it('escolhe a maior nota média', () => {
    expect(escolherDestaque([jogo(1, 4.1, 5), jogo(2, 4.8, 3), jogo(3, 3.9, 9)])?.id).toBe(2)
  })

  it('desempata pelo maior número de avaliações', () => {
    expect(escolherDestaque([jogo(1, 4.5, 2), jogo(2, 4.5, 7), jogo(3, 4.5, 4)])?.id).toBe(2)
  })

  it('ignora jogos sem avaliações', () => {
    expect(escolherDestaque([jogo(1, null, 0), jogo(2, 2.0, 1)])?.id).toBe(2)
  })

  it('devolve nulo quando ninguém foi avaliado ou a lista é vazia', () => {
    expect(escolherDestaque([jogo(1, null, 0), jogo(2, null, 0)])).toBeNull()
    expect(escolherDestaque([])).toBeNull()
  })

  it('prefere jogos com ao menos 3 avaliações a uma nota alta isolada', () => {
    expect(escolherDestaque([jogo(1, 5.0, 1), jogo(2, 5.0, 2), jogo(3, 4.6, 5)])?.id).toBe(3)
  })

  it('se nenhum jogo tem 3 avaliações, considera todos os avaliados', () => {
    expect(escolherDestaque([jogo(1, 4.0, 1), jogo(2, 4.9, 2)])?.id).toBe(2)
  })
})
