import type { ButtonHTMLAttributes } from 'react'

type Variante = 'primary' | 'ghost' | 'danger'

const VARIANTES: Record<Variante, string> = {
  primary: 'bg-vapor text-bg font-semibold hover:brightness-110',
  ghost: 'border border-line text-ink hover:bg-panel-2',
  danger: 'border border-danger/50 text-danger hover:bg-danger/10',
}

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variante
}

export function Button({ variant = 'primary', className = '', type = 'button', ...resto }: ButtonProps) {
  return (
    <button
      type={type}
      className={`inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2 text-sm transition disabled:cursor-not-allowed disabled:opacity-50 ${VARIANTES[variant]} ${className}`}
      {...resto}
    />
  )
}
