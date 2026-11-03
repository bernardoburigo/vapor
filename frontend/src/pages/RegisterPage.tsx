import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { Alert } from '../components/Alert'
import { AuthCard } from '../components/AuthCard'
import { Button } from '../components/Button'
import { FormField, inputClass } from '../components/FormField'

export function RegisterPage() {
  const { cadastrar, logado } = useAuth()
  const local = useLocation()
  const destino = (local.state as { from?: string } | null)?.from ?? '/'

  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState<Error | null>(null)
  const [enviando, setEnviando] = useState(false)

  if (logado) return <Navigate to={destino} replace />

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    setEnviando(true)
    try {
      // Cria a conta (sempre com perfil USER) e já entra com ela.
      await cadastrar({ nome, email, senha })
    } catch (e) {
      setErro(e instanceof Error ? e : new Error('Erro inesperado'))
      setEnviando(false)
    }
  }

  const campos = erro instanceof ApiError ? erro.fieldErrors : null

  return (
    <AuthCard
      titulo="Criar conta"
      subtitulo="É grátis e leva menos de um minuto."
      rodape={
        <>
          Já tem conta?{' '}
          <Link to="/login" state={local.state} className="text-cyan hover:underline">
            Entrar
          </Link>
        </>
      }
    >
      <form onSubmit={enviar} className="space-y-4" noValidate>
        <FormField label="Nome" htmlFor="nome" erro={campos?.nome}>
          <input
            id="nome"
            autoComplete="name"
            autoFocus
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            disabled={enviando}
            className={inputClass}
          />
        </FormField>
        <FormField label="E-mail" htmlFor="email" erro={campos?.email}>
          <input
            id="email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={enviando}
            className={inputClass}
          />
        </FormField>
        <FormField label="Senha" htmlFor="senha" erro={campos?.senha} dica="Entre 6 e 100 caracteres.">
          <input
            id="senha"
            type="password"
            autoComplete="new-password"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            disabled={enviando}
            className={inputClass}
          />
        </FormField>
        {erro && !campos && <Alert>{erro.message}</Alert>}
        <Button type="submit" className="w-full" disabled={enviando}>
          {enviando ? 'Criando conta...' : 'Criar conta'}
        </Button>
      </form>
    </AuthCard>
  )
}
