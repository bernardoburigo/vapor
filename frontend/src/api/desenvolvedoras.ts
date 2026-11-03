import { request } from './client'
import type { Desenvolvedora, DesenvolvedoraInput } from './types'

export const listarDesenvolvedoras = () => request<Desenvolvedora[]>('/desenvolvedoras')

export const criarDesenvolvedora = (dados: DesenvolvedoraInput) =>
  request<Desenvolvedora>('/desenvolvedoras', { method: 'POST', body: dados })

export const atualizarDesenvolvedora = (id: number, dados: DesenvolvedoraInput) =>
  request<Desenvolvedora>(`/desenvolvedoras/${id}`, { method: 'PUT', body: dados })

export const removerDesenvolvedora = (id: number) => request<void>(`/desenvolvedoras/${id}`, { method: 'DELETE' })
