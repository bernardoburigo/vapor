import { useEffect, useRef, type ReactNode } from 'react'

interface ModalProps {
  aberto: boolean
  titulo: string
  onClose: () => void
  children: ReactNode
}

/**
 * Usa o <dialog> nativo: ele já cuida do foco, da tecla Esc e do fundo. O conteúdo só é montado
 * enquanto aberto, então o formulário de dentro começa limpo a cada abertura.
 */
export function Modal({ aberto, titulo, onClose, children }: ModalProps) {
  const ref = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const dialogo = ref.current
    if (!dialogo) return
    if (aberto && !dialogo.open) dialogo.showModal()
    if (!aberto && dialogo.open) dialogo.close()
  }, [aberto])

  return (
    <dialog
      ref={ref}
      aria-label={titulo}
      onClose={onClose}
      onClick={(evento) => {
        // clique no fundo escurecido (o próprio <dialog>) fecha
        if (evento.target === ref.current) onClose()
      }}
      className="m-auto w-[calc(100%-2rem)] max-w-xl rounded-2xl border border-line bg-panel p-0 text-ink backdrop:bg-black/70"
    >
      {aberto && (
        <div className="max-h-[85vh] overflow-y-auto p-6">
          <div className="mb-4 flex items-start justify-between gap-4">
            <h2 className="text-lg font-semibold">{titulo}</h2>
            <button type="button" onClick={onClose} aria-label="Fechar" className="rounded p-1 text-muted hover:text-ink">
              ✕
            </button>
          </div>
          {children}
        </div>
      )}
    </dialog>
  )
}
