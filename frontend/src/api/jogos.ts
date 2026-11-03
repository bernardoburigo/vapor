import { buildQuery, request } from './client'
import type { Jogo, JogoFiltro, JogoInput, PageResponse } from './types'

export const listarJogos = (filtro: JogoFiltro = {}) =>
  request<PageResponse<Jogo>>(`/jogos${buildQuery({ ...filtro })}`)

export const buscarJogo = (id: number) => request<Jogo>(`/jogos/${id}`)

export const criarJogo = (dados: JogoInput) => request<Jogo>('/jogos', { method: 'POST', body: dados })

export const atualizarJogo = (id: number, dados: JogoInput) =>
  request<Jogo>(`/jogos/${id}`, { method: 'PUT', body: dados })

export const removerJogo = (id: number) => request<void>(`/jogos/${id}`, { method: 'DELETE' })
