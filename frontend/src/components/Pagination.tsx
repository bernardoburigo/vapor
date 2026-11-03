import { Button } from './Button'

interface PaginationProps {
  /** Página atual, começando em 1. */
  pagina: number
  totalPaginas: number
  onChange: (pagina: number) => void
}

/** Janela de até 5 números ao redor da página atual. */
function janela(pagina: number, total: number): number[] {
  const inicio = Math.max(1, Math.min(pagina - 2, total - 4))
  const fim = Math.min(total, inicio + 4)
  return Array.from({ length: fim - inicio + 1 }, (_, i) => inicio + i)
}

export function Pagination({ pagina, totalPaginas, onChange }: PaginationProps) {
  if (totalPaginas <= 1) return null
  return (
    <nav aria-label="Paginação" className="flex flex-wrap items-center justify-center gap-1.5">
      <Button variant="ghost" disabled={pagina <= 1} onClick={() => onChange(pagina - 1)}>
        Anterior
      </Button>
      {janela(pagina, totalPaginas).map((n) => (
        <Button
          key={n}
          variant={n === pagina ? 'primary' : 'ghost'}
          aria-current={n === pagina ? 'page' : undefined}
          aria-label={`Página ${n}`}
          className="min-w-9"
          onClick={() => onChange(n)}
        >
          {n}
        </Button>
      ))}
      <Button variant="ghost" disabled={pagina >= totalPaginas} onClick={() => onChange(pagina + 1)}>
        Próxima
      </Button>
    </nav>
  )
}
