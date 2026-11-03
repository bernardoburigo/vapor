import { Link } from 'react-router-dom'
import type { Jogo } from '../api/types'
import { formatPrice, formatRating } from '../lib/format'
import { GameCover } from './GameCover'
import { Stars } from './Stars'

export function GameCard({ jogo }: { jogo: Jogo }) {
  const extras = jogo.categorias.length - 2
  return (
    <Link
      to={`/jogos/${jogo.id}`}
      className="group flex flex-col overflow-hidden rounded-xl border border-line bg-panel transition hover:-translate-y-1 hover:border-cyan/60 hover:shadow-[0_8px_30px_rgba(76,201,240,0.12)]"
    >
      <GameCover titulo={jogo.titulo} className="aspect-[4/3] text-5xl" />
      <div className="flex flex-1 flex-col gap-2 p-3">
        <h3 className="line-clamp-2 font-semibold leading-snug group-hover:text-cyan">{jogo.titulo}</h3>
        <p className="truncate text-xs text-muted">{jogo.desenvolvedora.nome}</p>
        <div className="flex flex-wrap gap-1">
          {jogo.categorias.slice(0, 2).map((c) => (
            <span key={c.id} className="rounded bg-panel-2 px-1.5 py-0.5 text-[11px] text-muted">
              {c.nome}
            </span>
          ))}
          {extras > 0 && <span className="rounded bg-panel-2 px-1.5 py-0.5 text-[11px] text-muted">+{extras}</span>}
        </div>
        <div className="mt-auto flex flex-wrap items-center justify-between gap-x-2 gap-y-1.5 pt-2">
          <span className="flex items-center gap-1.5 text-xs text-muted">
            <Stars value={jogo.notaMedia} className="h-3.5" />
            {formatRating(jogo.notaMedia)}
          </span>
          <span className="shrink-0 rounded bg-panel-2 px-2 py-1 text-sm font-semibold">{formatPrice(jogo.preco)}</span>
        </div>
      </div>
    </Link>
  )
}

export function GameCardSkeleton() {
  return (
    <div className="animate-pulse overflow-hidden rounded-xl border border-line bg-panel" aria-hidden="true">
      <div className="aspect-[4/3] bg-panel-2" />
      <div className="space-y-2 p-3">
        <div className="h-4 w-3/4 rounded bg-panel-2" />
        <div className="h-3 w-1/2 rounded bg-panel-2" />
        <div className="h-3 w-1/3 rounded bg-panel-2" />
      </div>
    </div>
  )
}
