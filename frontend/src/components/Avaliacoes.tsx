import { useState, type FormEvent } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { alterarAvaliacao, avaliarJogo, removerAvaliacao } from '../api/avaliacoes'
import { ApiError } from '../api/client'
import type { Avaliacao } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { formatDate } from '../lib/format'
import { Alert } from './Alert'
import { Button } from './Button'
import { ConfirmButton } from './ConfirmButton'
import { FormField, inputClass } from './FormField'
import { Stars, StarInput } from './Stars'

const LIMITE_COMENTARIO = 2000

interface AvaliacoesProps {
  jogoId: number
  avaliacoes: Avaliacao[]
  /** Chamado depois de criar, alterar ou remover, para a tela recarregar a lista e a nota média. */
  onChange: () => void
}

export function Avaliacoes({ jogoId, avaliacoes, onChange }: AvaliacoesProps) {
  const { usuario, logado, isAdmin } = useAuth()
  const local = useLocation()
  const [editando, setEditando] = useState(false)
  const [erroRemocao, setErroRemocao] = useState<string | null>(null)

  const minha = avaliacoes.find((a) => a.usuarioId === usuario?.id)
  const outras = avaliacoes
    .filter((a) => a.id !== minha?.id)
    .sort((a, b) => b.dataAvaliacao.localeCompare(a.dataAvaliacao))

  async function remover(id: number) {
    setErroRemocao(null)
    try {
      await removerAvaliacao(id)
      onChange()
    } catch (erro) {
      setErroRemocao(erro instanceof Error ? erro.message : 'Não foi possível remover a avaliação.')
    }
  }

  return (
    <section aria-labelledby="titulo-avaliacoes" className="space-y-5">
      <h2 id="titulo-avaliacoes" className="text-xl font-bold">
        Avaliações
      </h2>

      {!logado && (
        <div className="rounded-xl border border-line bg-panel p-5">
          <p className="font-medium">Jogou este jogo?</p>
          <p className="mt-1 text-sm text-muted">Entre na sua conta para dar uma nota e deixar um comentário.</p>
          <div className="mt-4 flex gap-2">
            <Link
              to="/login"
              state={{ from: local.pathname }}
              className="rounded-lg bg-vapor px-4 py-2 text-sm font-semibold text-bg hover:brightness-110"
            >
              Entrar
            </Link>
            <Link
              to="/cadastro"
              state={{ from: local.pathname }}
              className="rounded-lg border border-line px-4 py-2 text-sm hover:bg-panel-2"
            >
              Criar conta
            </Link>
          </div>
        </div>
      )}

      {logado && !minha && (
        <FormularioAvaliacao
          titulo="Avalie este jogo"
          rotuloEnvio="Publicar avaliação"
          onSalvar={async (nota, comentario) => {
            await avaliarJogo(jogoId, nota, comentario)
            onChange()
          }}
        />
      )}

      {minha && (
        <div className="rounded-xl border border-cyan/40 bg-panel p-5">
          {editando ? (
            <FormularioAvaliacao
              titulo="Editar sua avaliação"
              rotuloEnvio="Salvar alterações"
              inicial={minha}
              onCancelar={() => setEditando(false)}
              onSalvar={async (nota, comentario) => {
                await alterarAvaliacao(minha.id, nota, comentario)
                setEditando(false)
                onChange()
              }}
            />
          ) : (
            <>
              <p className="text-xs font-semibold uppercase tracking-widest text-cyan">Sua avaliação</p>
              <Cabecalho avaliacao={minha} />
              {minha.comentario && <p className="mt-2 whitespace-pre-line text-sm text-ink/90">{minha.comentario}</p>}
              <div className="mt-4 flex flex-wrap items-center gap-2">
                <Button variant="ghost" onClick={() => setEditando(true)}>
                  Editar
                </Button>
                <ConfirmButton rotulo="Remover" pergunta="Remover sua avaliação?" onConfirm={() => remover(minha.id)} />
              </div>
            </>
          )}
        </div>
      )}

      {erroRemocao && <Alert>{erroRemocao}</Alert>}

      {outras.length === 0 && !minha ? (
        <p className="rounded-xl border border-dashed border-line p-6 text-center text-sm text-muted">
          Este jogo ainda não tem avaliações. Seja o primeiro a avaliar!
        </p>
      ) : (
        <ul className="space-y-3">
          {outras.map((avaliacao) => (
            <li key={avaliacao.id} className="rounded-xl border border-line bg-panel p-4">
              <Cabecalho avaliacao={avaliacao} />
              {avaliacao.comentario && (
                <p className="mt-2 whitespace-pre-line text-sm text-ink/90">{avaliacao.comentario}</p>
              )}
              {isAdmin && (
                <div className="mt-3">
                  <ConfirmButton
                    rotulo="Remover"
                    pergunta="Remover esta avaliação (moderação)?"
                    onConfirm={() => remover(avaliacao.id)}
                  />
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

function Cabecalho({ avaliacao }: { avaliacao: Avaliacao }) {
  return (
    <div className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1">
      <span className="font-semibold">{avaliacao.usuarioNome}</span>
      <Stars value={avaliacao.nota} className="h-4" />
      <span className="text-xs text-muted">{formatDate(avaliacao.dataAvaliacao)}</span>
    </div>
  )
}

interface FormularioProps {
  titulo: string
  rotuloEnvio: string
  inicial?: Avaliacao
  onSalvar: (nota: number, comentario: string | null) => Promise<void>
  onCancelar?: () => void
}

function FormularioAvaliacao({ titulo, rotuloEnvio, inicial, onSalvar, onCancelar }: FormularioProps) {
  const [nota, setNota] = useState(inicial?.nota ?? 0)
  const [comentario, setComentario] = useState(inicial?.comentario ?? '')
  const [erro, setErro] = useState<ApiError | Error | null>(null)
  const [erroNota, setErroNota] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    if (nota < 1) {
      setErroNota('Escolha uma nota de 1 a 5 estrelas.')
      return
    }
    setErroNota(null)
    setErro(null)
    setEnviando(true)
    try {
      await onSalvar(nota, comentario.trim() || null)
    } catch (e) {
      setErro(e instanceof Error ? e : new Error('Erro inesperado'))
    } finally {
      setEnviando(false)
    }
  }

  const campos = erro instanceof ApiError ? erro.fieldErrors : null

  return (
    <form onSubmit={enviar} className="space-y-4 rounded-xl border border-line bg-panel p-5" noValidate>
      <h3 className="font-semibold">{titulo}</h3>

      <FormField label="Sua nota" htmlFor="nota" erro={erroNota ?? campos?.nota}>
        <div id="nota">
          <StarInput value={nota} onChange={setNota} disabled={enviando} />
        </div>
      </FormField>

      <FormField
        label="Comentário (opcional)"
        htmlFor="comentario"
        erro={campos?.comentario}
        dica={`${comentario.length}/${LIMITE_COMENTARIO} caracteres`}
      >
        <textarea
          id="comentario"
          rows={4}
          maxLength={LIMITE_COMENTARIO}
          value={comentario}
          onChange={(e) => setComentario(e.target.value)}
          disabled={enviando}
          placeholder="O que você achou do jogo?"
          className={inputClass}
        />
      </FormField>

      {erro && !campos && <Alert>{erro.message}</Alert>}

      <div className="flex flex-wrap gap-2">
        <Button type="submit" disabled={enviando}>
          {enviando ? 'Enviando...' : rotuloEnvio}
        </Button>
        {onCancelar && (
          <Button variant="ghost" onClick={onCancelar} disabled={enviando}>
            Cancelar
          </Button>
        )}
      </div>
    </form>
  )
}
