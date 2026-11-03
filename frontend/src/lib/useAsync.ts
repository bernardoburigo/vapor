import { useCallback, useEffect, useState } from 'react'

export interface AsyncState<T> {
  data: T | null
  error: Error | null
  loading: boolean
  reload: () => void
}

/**
 * Executa uma chamada assíncrona sempre que `deps` mudar. Mantém o dado anterior enquanto carrega o novo
 * (a tela não pisca) e descarta respostas de chamadas que já ficaram obsoletas.
 */
export function useAsync<T>(carregar: () => Promise<T>, deps: unknown[]): AsyncState<T> {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<Error | null>(null)
  const [loading, setLoading] = useState(true)
  const [tentativa, setTentativa] = useState(0)

  useEffect(() => {
    let obsoleta = false
    setLoading(true)
    carregar()
      .then((resultado) => {
        if (obsoleta) return
        setData(resultado)
        setError(null)
      })
      .catch((erro: unknown) => {
        if (obsoleta) return
        setError(erro instanceof Error ? erro : new Error(String(erro)))
      })
      .finally(() => {
        if (!obsoleta) setLoading(false)
      })
    return () => {
      obsoleta = true
    }
  }, [...deps, tentativa])

  const reload = useCallback(() => setTentativa((n) => n + 1), [])

  return { data, error, loading, reload }
}
