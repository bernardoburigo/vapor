import { useState } from 'react'
import { formatRating } from '../lib/format'

function Estrela() {
  return (
    <svg viewBox="0 0 20 20" className="h-full w-auto shrink-0" aria-hidden="true">
      <path d="M10 1.5l2.6 5.5 6 .8-4.4 4.2 1.1 6L10 15.1 4.7 18l1.1-6L1.4 7.8l6-.8z" fill="currentColor" />
    </svg>
  )
}

const CINCO = [0, 1, 2, 3, 4]

/** Cinco estrelas preenchidas proporcionalmente à nota (aceita fração, como 4,5). */
export function Stars({ value, className = 'h-4' }: { value: number | null; className?: string }) {
  const porcentagem = value == null ? 0 : (Math.max(0, Math.min(5, value)) / 5) * 100
  return (
    <span
      role="img"
      aria-label={value == null ? 'Sem avaliações' : `Nota ${formatRating(value)} de 5`}
      className={`relative inline-flex ${className}`}
    >
      <span className="flex text-line">
        {CINCO.map((i) => (
          <Estrela key={i} />
        ))}
      </span>
      <span className="absolute inset-y-0 left-0 flex overflow-hidden text-star" style={{ width: `${porcentagem}%` }}>
        {CINCO.map((i) => (
          <Estrela key={i} />
        ))}
      </span>
    </span>
  )
}

interface StarInputProps {
  value: number
  onChange: (nota: number) => void
  disabled?: boolean
}

/** Seletor de nota de 1 a 5, com pré-visualização ao passar o mouse. */
export function StarInput({ value, onChange, disabled = false }: StarInputProps) {
  const [previa, setPrevia] = useState(0)
  const ativa = previa || value

  return (
    <div role="radiogroup" aria-label="Nota de 1 a 5" className="inline-flex gap-1" onMouseLeave={() => setPrevia(0)}>
      {CINCO.map((i) => {
        const nota = i + 1
        return (
          <button
            key={nota}
            type="button"
            role="radio"
            aria-checked={value === nota}
            aria-label={`${nota} ${nota === 1 ? 'estrela' : 'estrelas'}`}
            disabled={disabled}
            onClick={() => onChange(nota)}
            onMouseEnter={() => setPrevia(nota)}
            className={`h-8 rounded p-0.5 transition ${nota <= ativa ? 'text-star' : 'text-line hover:text-muted'}`}
          >
            <Estrela />
          </button>
        )
      })}
    </div>
  )
}
