import { useState } from 'react'
import { Button } from './Button'

interface ConfirmButtonProps {
  rotulo: string
  /** Pergunta mostrada no segundo passo, antes de executar. */
  pergunta?: string
  onConfirm: () => void | Promise<void>
  disabled?: boolean
}

/** Botão destrutivo em dois passos ("Remover" -> "Confirmar?"), sem precisar de um modal. */
export function ConfirmButton({ rotulo, pergunta = 'Tem certeza?', onConfirm, disabled }: ConfirmButtonProps) {
  const [confirmando, setConfirmando] = useState(false)

  if (!confirmando) {
    return (
      <Button variant="danger" disabled={disabled} onClick={() => setConfirmando(true)}>
        {rotulo}
      </Button>
    )
  }

  return (
    <span className="inline-flex flex-wrap items-center gap-2" role="group" aria-label={pergunta}>
      <span className="text-sm text-muted">{pergunta}</span>
      <Button
        variant="danger"
        disabled={disabled}
        onClick={async () => {
          setConfirmando(false)
          await onConfirm()
        }}
      >
        Sim, {rotulo.toLowerCase()}
      </Button>
      <Button variant="ghost" onClick={() => setConfirmando(false)}>
        Cancelar
      </Button>
    </span>
  )
}
