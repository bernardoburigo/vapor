import { Link } from 'react-router-dom'
import type { Jogo } from '../api/types'
import { coverFor, coverGradient } from '../lib/cover'
import { formatPrice, formatRating, pluralize } from '../lib/format'
import { GameCover } from './GameCover'
import { Stars } from './Stars'

/** Banner do jogo mais bem avaliado, no topo da vitrine. */
export function Destaque({ jogo }: { jogo: Jogo }) {
  return (
    <section
      aria-label="Jogo em destaque"
      className="relative overflow-hidden rounded-2xl border border-line"
      style={{ backgroundImage: coverGradient(coverFor(jogo.titulo)) }}
    >
      <div className="absolute inset-0 bg-gradient-to-r from-bg/95 via-bg/65 to-transparent" />
      <div className="relative grid items-center gap-6 p-6 md:grid-cols-[1fr_auto] md:p-10">
        <div className="max-w-xl space-y-3">
          <p className="text-xs font-semibold uppercase tracking-widest text-cyan">Mais bem avaliado</p>
          <h2 className="text-3xl font-black leading-tight md:text-4xl">{jogo.titulo}</h2>
          <div className="flex flex-wrap items-center gap-2">
            <Stars value={jogo.notaMedia} className="h-5" />
            <span className="font-semibold">{formatRating(jogo.notaMedia)}</span>
            <span className="text-sm text-muted">({pluralize(jogo.totalAvaliacoes, 'avaliação', 'avaliações')})</span>
          </div>
          {jogo.descricao && <p className="line-clamp-2 text-sm text-ink/80">{jogo.descricao}</p>}
          <div className="flex flex-wrap gap-1.5">
            <span className="rounded bg-bg/60 px-2 py-0.5 text-xs text-muted">{jogo.desenvolvedora.nome}</span>
            {jogo.categorias.map((c) => (
              <span key={c.id} className="rounded bg-bg/60 px-2 py-0.5 text-xs text-muted">
                {c.nome}
              </span>
            ))}
          </div>
          <div className="flex items-center gap-4 pt-2">
            <Link
              to={`/jogos/${jogo.id}`}
              className="inline-flex rounded-lg bg-vapor px-5 py-2.5 text-sm font-semibold text-bg hover:brightness-110"
            >
              Ver jogo
            </Link>
            <span className="text-lg font-semibold">{formatPrice(jogo.preco)}</span>
          </div>
        </div>
        <GameCover titulo={jogo.titulo} className="hidden aspect-[4/5] w-48 rounded-xl text-6xl shadow-2xl md:flex" />
      </div>
    </section>
  )
}
