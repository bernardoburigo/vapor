import { coverFor, coverGradient } from '../lib/cover'

interface GameCoverProps {
  titulo: string
  /** Controla o tamanho/proporção e o tamanho das iniciais; ex.: "aspect-[4/5] text-5xl". */
  className?: string
}

/** Capa gerada a partir do título (o backend não tem imagens). Decorativa: o título aparece em texto ao lado. */
export function GameCover({ titulo, className = 'aspect-[4/5] text-5xl' }: GameCoverProps) {
  const capa = coverFor(titulo)
  return (
    <div
      aria-hidden="true"
      className={`relative flex select-none items-center justify-center overflow-hidden font-black tracking-wider text-white/90 ${className}`}
      style={{ backgroundImage: coverGradient(capa) }}
    >
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,rgba(255,255,255,0.22),transparent_55%)]" />
      <span className="relative drop-shadow-[0_2px_8px_rgba(0,0,0,0.45)]">{capa.initials}</span>
    </div>
  )
}
