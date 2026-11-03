import type { ReactNode } from 'react'

/** Aparência dos campos, sem largura (selects de filtro usam esta e definem a própria largura). */
export const inputBase =
  'rounded-lg border border-line bg-bg px-3 py-2 text-sm text-ink placeholder:text-muted/70 focus:border-cyan disabled:opacity-60'

/** Campo de formulário: ocupa toda a largura disponível. */
export const inputClass = `w-full ${inputBase}`

interface FormFieldProps {
  label: string
  htmlFor: string
  /** Mensagem de erro do backend para este campo (fieldErrors). */
  erro?: string
  dica?: string
  children: ReactNode
}

export function FormField({ label, htmlFor, erro, dica, children }: FormFieldProps) {
  return (
    <div className="space-y-1.5">
      <label htmlFor={htmlFor} className="block text-sm font-medium">
        {label}
      </label>
      {children}
      {dica && !erro && <p className="text-xs text-muted">{dica}</p>}
      {erro && (
        <p role="alert" className="text-xs text-danger">
          {erro}
        </p>
      )}
    </div>
  )
}
