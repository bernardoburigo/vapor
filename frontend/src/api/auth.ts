import { request } from './client'
import type { AuthResponse, RegistroInput, Usuario } from './types'

export const login = (email: string, senha: string) =>
  request<AuthResponse>('/usuarios/login', { method: 'POST', body: { email, senha }, auth: false })

export const registrar = (dados: RegistroInput) =>
  request<Usuario>('/usuarios', { method: 'POST', body: dados, auth: false })
