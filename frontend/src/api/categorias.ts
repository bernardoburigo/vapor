import { request } from './client'
import type { Categoria, CategoriaInput } from './types'

export const listarCategorias = () => request<Categoria[]>('/categorias')

export const criarCategoria = (dados: CategoriaInput) =>
  request<Categoria>('/categorias', { method: 'POST', body: dados })

export const atualizarCategoria = (id: number, dados: CategoriaInput) =>
  request<Categoria>(`/categorias/${id}`, { method: 'PUT', body: dados })

export const removerCategoria = (id: number) => request<void>(`/categorias/${id}`, { method: 'DELETE' })
