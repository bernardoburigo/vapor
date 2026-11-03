import { buildQuery, request } from './client'
import type { Avaliacao } from './types'

export const listarAvaliacoesDoJogo = (jogoId: number) =>
  request<Avaliacao[]>(`/avaliacoes${buildQuery({ jogoId })}`)

/** O autor é o usuário do token; o corpo leva só o jogo, a nota e o comentário. */
export const avaliarJogo = (jogoId: number, nota: number, comentario: string | null) =>
  request<Avaliacao>('/avaliacoes', { method: 'POST', body: { jogoId, nota, comentario } })

export const alterarAvaliacao = (id: number, nota: number, comentario: string | null) =>
  request<Avaliacao>(`/avaliacoes/${id}`, { method: 'PUT', body: { nota, comentario } })

export const removerAvaliacao = (id: number) => request<void>(`/avaliacoes/${id}`, { method: 'DELETE' })
