import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import * as authApi from '../api/auth'
import { configureClient } from '../api/client'
import type { RegistroInput, Usuario } from '../api/types'

interface Sessao {
  token: string
  usuario: Usuario
  /** Instante (ms desde 1970) em que o token deixa de valer. */
  expiraEm: number
}

interface AuthContextValue {
  usuario: Usuario | null
  logado: boolean
  isAdmin: boolean
  /** Mensagem a exibir quando a sessão terminou sem o usuário pedir (token expirado ou recusado). */
  aviso: string | null
  entrar: (email: string, senha: string) => Promise<Usuario>
  cadastrar: (dados: RegistroInput) => Promise<Usuario>
  sair: () => void
  limparAviso: () => void
}

const CHAVE = 'vapor.sessao'
const AVISO_EXPIRADA = 'Sua sessão expirou. Entre novamente para continuar.'

function lerSessao(): Sessao | null {
  try {
    const bruto = localStorage.getItem(CHAVE)
    if (!bruto) return null
    const sessao = JSON.parse(bruto) as Sessao
    if (!sessao.token || !sessao.usuario || sessao.expiraEm <= Date.now()) {
      localStorage.removeItem(CHAVE)
      return null
    }
    return sessao
  } catch {
    return null
  }
}

function guardarSessao(sessao: Sessao | null): void {
  try {
    if (sessao) localStorage.setItem(CHAVE, JSON.stringify(sessao))
    else localStorage.removeItem(CHAVE)
  } catch {
    // armazenamento indisponível (modo privado): a sessão vale só até recarregar a página
  }
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Sessao | null>(lerSessao)
  const [aviso, setAviso] = useState<string | null>(null)
  const sessaoRef = useRef(sessao)
  sessaoRef.current = sessao

  const encerrar = useCallback((mensagem: string | null) => {
    guardarSessao(null)
    setSessao(null)
    setAviso(mensagem)
  }, [])

  // O cliente HTTP lê o token da sessão atual e, se a API recusar o token, encerra a sessão.
  useEffect(() => {
    configureClient({
      getToken: () => sessaoRef.current?.token ?? null,
      onUnauthorized: () => encerrar(AVISO_EXPIRADA),
    })
  }, [encerrar])

  // Encerra sozinho no instante em que o token vence, mesmo com a página aberta.
  useEffect(() => {
    if (!sessao) return
    const restante = Math.min(sessao.expiraEm - Date.now(), 2_000_000_000)
    const timer = window.setTimeout(() => encerrar(AVISO_EXPIRADA), Math.max(restante, 0))
    return () => window.clearTimeout(timer)
  }, [sessao, encerrar])

  const entrar = useCallback(async (email: string, senha: string) => {
    const resposta = await authApi.login(email, senha)
    const nova: Sessao = {
      token: resposta.token,
      usuario: resposta.usuario,
      expiraEm: Date.now() + resposta.expiraEmSegundos * 1000,
    }
    guardarSessao(nova)
    setSessao(nova)
    setAviso(null)
    return resposta.usuario
  }, [])

  const cadastrar = useCallback(
    async (dados: RegistroInput) => {
      await authApi.registrar(dados)
      return entrar(dados.email, dados.senha)
    },
    [entrar],
  )

  const value = useMemo<AuthContextValue>(
    () => ({
      usuario: sessao?.usuario ?? null,
      logado: sessao !== null,
      isAdmin: sessao?.usuario.role === 'ADMIN',
      aviso,
      entrar,
      cadastrar,
      sair: () => encerrar(null),
      limparAviso: () => setAviso(null),
    }),
    [sessao, aviso, entrar, cadastrar, encerrar],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const contexto = useContext(AuthContext)
  if (!contexto) throw new Error('useAuth precisa estar dentro de <AuthProvider>')
  return contexto
}
