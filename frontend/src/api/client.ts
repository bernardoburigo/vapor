const BASE_URL = String(import.meta.env.VITE_API_URL ?? 'http://localhost:8081/api').replace(/\/+$/, '')

/** Erro devolvido pela API, no formato { status, message, fieldErrors } do GlobalExceptionHandler. */
export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string> | null

  constructor(status: number, message: string, fieldErrors: Record<string, string> | null = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

interface ClientConfig {
  getToken: () => string | null
  onUnauthorized: () => void
}

let config: ClientConfig = { getToken: () => null, onUnauthorized: () => {} }

/** Liga o cliente à sessão: de onde ler o token e o que fazer quando ele for recusado. */
export function configureClient(novo: ClientConfig): void {
  config = novo
}

type Valor = string | number | null | undefined

export function buildQuery(params: Record<string, Valor>): string {
  const busca = new URLSearchParams()
  for (const [chave, valor] of Object.entries(params)) {
    if (valor === undefined || valor === null || valor === '') continue
    busca.set(chave, String(valor))
  }
  const texto = busca.toString()
  return texto ? `?${texto}` : ''
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
  /** false em login e cadastro: nunca envia token e um 401 ali é "senha errada", não "sessão expirada". */
  auth?: boolean
}

export async function request<T>(path: string, { method = 'GET', body, auth = true }: RequestOptions = {}): Promise<T> {
  const token = auth ? config.getToken() : null
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  let resposta: Response
  try {
    resposta = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor. Verifique se o backend está no ar.')
  }

  if (resposta.ok) {
    return (resposta.status === 204 ? undefined : await resposta.json()) as T
  }

  const erro = await lerErro(resposta)
  if (resposta.status === 401 && token) config.onUnauthorized()
  throw erro
}

async function lerErro(resposta: Response): Promise<ApiError> {
  try {
    const corpo = (await resposta.json()) as { message?: string; fieldErrors?: Record<string, string> | null }
    return new ApiError(resposta.status, corpo.message ?? `Erro ${resposta.status}`, corpo.fieldErrors ?? null)
  } catch {
    return new ApiError(resposta.status, `Erro ${resposta.status} ao falar com o servidor`)
  }
}
