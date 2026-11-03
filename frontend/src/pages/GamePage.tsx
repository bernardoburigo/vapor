import { Link, useParams } from 'react-router-dom'
import { listarAvaliacoesDoJogo } from '../api/avaliacoes'
import { ApiError } from '../api/client'
import { buscarJogo } from '../api/jogos'
import { Alert } from '../components/Alert'
import { Avaliacoes } from '../components/Avaliacoes'
import { Button } from '../components/Button'
import { GameCover } from '../components/GameCover'
import { Stars } from '../components/Stars'
import { formatDate, formatPrice, formatRating, pluralize } from '../lib/format'
import { useAsync } from '../lib/useAsync'
import { NotFoundPage } from './NotFoundPage'

export function GamePage() {
  const { id } = useParams()
  const jogoId = Number(id)
  const idValido = Number.isInteger(jogoId) && jogoId > 0

  const jogo = useAsync(() => (idValido ? buscarJogo(jogoId) : Promise.reject(new ApiError(404, ''))), [jogoId])
  const avaliacoes = useAsync(() => (idValido ? listarAvaliacoesDoJogo(jogoId) : Promise.resolve([])), [jogoId])

  function recarregar() {
    jogo.reload()
    avaliacoes.reload()
  }

  if (!idValido || (jogo.error instanceof ApiError && jogo.error.status === 404)) {
    return <NotFoundPage />
  }

  if (jogo.error && !jogo.data) {
    return (
      <div className="space-y-3">
        <Alert>{jogo.error.message}</Alert>
        <Button variant="ghost" onClick={recarregar}>
          Tentar de novo
        </Button>
      </div>
    )
  }

  if (!jogo.data) {
    return (
      <div className="grid animate-pulse gap-8 md:grid-cols-[320px_1fr]" aria-busy="true" aria-label="Carregando jogo">
        <div className="aspect-[4/5] rounded-2xl bg-panel" />
        <div className="space-y-4">
          <div className="h-9 w-2/3 rounded bg-panel" />
          <div className="h-4 w-1/3 rounded bg-panel" />
          <div className="h-24 rounded bg-panel" />
        </div>
      </div>
    )
  }

  const g = jogo.data

  return (
    <div className="space-y-10">
      <Link to="/" className="inline-block text-sm text-muted hover:text-ink">
        ← Voltar à loja
      </Link>

      <div className="grid gap-8 md:grid-cols-[320px_1fr]">
        <GameCover titulo={g.titulo} className="aspect-[4/5] w-full rounded-2xl border border-line text-8xl" />

        <div className="space-y-5">
          <div>
            <h1 className="text-3xl font-black leading-tight md:text-4xl">{g.titulo}</h1>
            <p className="mt-1 text-sm text-muted">
              por{' '}
              <Link to={`/?desenvolvedoraId=${g.desenvolvedora.id}`} className="text-cyan hover:underline">
                {g.desenvolvedora.nome}
              </Link>
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            {g.notaMedia == null ? (
              <span className="text-sm text-muted">Ainda sem avaliações</span>
            ) : (
              <>
                <span className="text-4xl font-black text-star">{formatRating(g.notaMedia)}</span>
                <div>
                  <Stars value={g.notaMedia} className="h-5" />
                  <p className="text-xs text-muted">{pluralize(g.totalAvaliacoes, 'avaliação', 'avaliações')}</p>
                </div>
              </>
            )}
          </div>

          <div className="flex flex-wrap gap-2" aria-label="Categorias">
            {g.categorias.map((c) => (
              <Link
                key={c.id}
                to={`/?categoriaId=${c.id}`}
                className="rounded-full border border-line px-3 py-1 text-sm text-muted hover:border-cyan/60 hover:text-ink"
              >
                {c.nome}
              </Link>
            ))}
          </div>

          <dl className="grid grid-cols-2 gap-4 rounded-xl border border-line bg-panel p-4 text-sm sm:max-w-md">
            <div>
              <dt className="text-muted">Lançamento</dt>
              <dd className="mt-0.5 font-medium">{formatDate(g.dataLancamento)}</dd>
            </div>
            <div>
              <dt className="text-muted">Preço</dt>
              <dd className="mt-0.5 text-lg font-bold">{formatPrice(g.preco)}</dd>
            </div>
          </dl>

          {g.descricao && <p className="max-w-prose whitespace-pre-line leading-relaxed text-ink/90">{g.descricao}</p>}
        </div>
      </div>

      {avaliacoes.error && !avaliacoes.data ? (
        <Alert>{avaliacoes.error.message}</Alert>
      ) : (
        <Avaliacoes jogoId={g.id} avaliacoes={avaliacoes.data ?? []} onChange={recarregar} />
      )}
    </div>
  )
}
