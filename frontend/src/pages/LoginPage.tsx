import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { Alert } from '../components/Alert'
import { AuthCard } from '../components/AuthCard'
import { Button } from '../components/Button'
import { FormField, inputClass } from '../components/FormField'

export function LoginPage() {
  const { entrar, logado } = useAuth()
  const local = useLocation()
  const destino = (local.state as { from?: string } | null)?.from ?? '/'

  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState<Error | null>(null)
  const [enviando, setEnviando] = useState(false)

  // Depois de entrar (ou se já estava logado), volta para onde o usuário estava.
  if (logado) return <Navigate to={destino} replace />

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    setEnviando(true)
    try {
      await entrar(email, senha)
    } catch (e) {
      setErro(e instanceof Error ? e : new Error('Erro inesperado'))
      setEnviando(false)
    }
  }

  const campos = erro instanceof ApiError ? erro.fieldErrors : null

  return (
    <AuthCard
      titulo="Entrar"
      subtitulo="Acesse sua conta para avaliar jogos."
      rodape={
        <>
          Ainda não tem conta?{' '}
          <Link to="/cadastro" state={local.state} className="text-cyan hover:underline">
            Cadastre-se
          </Link>
        </>
      }
    >
      {/* noValidate: as mensagens de validação exibidas vêm do backend (fieldErrors). */}
      <form onSubmit={enviar} className="space-y-4" noValidate>
        <FormField label="E-mail" htmlFor="email" erro={campos?.email}>
          <input
            id="email"
            type="email"
            autoComplete="email"
            autoFocus
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={enviando}
            className={inputClass}
          />
        </FormField>
        <FormField label="Senha" htmlFor="senha" erro={campos?.senha}>
          <input
            id="senha"
            type="password"
            autoComplete="current-password"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            disabled={enviando}
            className={inputClass}
          />
        </FormField>
        {erro && !campos && <Alert>{erro.message}</Alert>}
        <Button type="submit" className="w-full" disabled={enviando}>
          {enviando ? 'Entrando...' : 'Entrar'}
        </Button>
      </form>
    </AuthCard>
  )
}
