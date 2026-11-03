import { useState } from 'react'
import { ApiError } from '../api/client'

/**
 * Estado comum dos formulários que gravam na API: se está enviando, o erro geral e os erros por campo
 * (fieldErrors do backend), prontos para serem exibidos abaixo de cada input.
 */
export function useSubmit() {
  const [erro, setErro] = useState<Error | null>(null)
  const [enviando, setEnviando] = useState(false)

  /** Executa a ação; devolve true se deu certo. */
  async function executar(acao: () => Promise<unknown>): Promise<boolean> {
    setErro(null)
    setEnviando(true)
    try {
      await acao()
      return true
    } catch (e) {
      setErro(e instanceof Error ? e : new Error('Erro inesperado'))
      return false
    } finally {
      setEnviando(false)
    }
  }

  const campos = erro instanceof ApiError ? erro.fieldErrors : null
  return { erro, campos, enviando, executar }
}
