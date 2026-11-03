import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError, buildQuery, configureClient, request } from './client'

function resposta(status: number, corpo?: unknown): Response {
  return new Response(corpo === undefined ? null : JSON.stringify(corpo), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('buildQuery', () => {
  it('monta a query ignorando valores vazios', () => {
    expect(buildQuery({ categoriaId: 3, titulo: '', page: 0, sort: undefined, x: null })).toBe('?categoriaId=3&page=0')
  })

  it('devolve texto vazio quando não há parâmetros', () => {
    expect(buildQuery({})).toBe('')
  })

  it('codifica caracteres especiais', () => {
    expect(buildQuery({ titulo: 'ação & cia' })).toBe('?titulo=a%C3%A7%C3%A3o+%26+cia')
  })
})

describe('request', () => {
  const fetchMock = vi.fn()
  const onUnauthorized = vi.fn()

  beforeEach(() => {
    fetchMock.mockReset()
    onUnauthorized.mockReset()
    vi.stubGlobal('fetch', fetchMock)
    configureClient({ getToken: () => 'token-abc', onUnauthorized })
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('envia o token no header Authorization', async () => {
    fetchMock.mockResolvedValue(resposta(200, { ok: true }))

    await request('/jogos')

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('http://localhost:8081/api/jogos')
    expect(init.headers.Authorization).toBe('Bearer token-abc')
  })

  it('não envia token quando auth é false', async () => {
    fetchMock.mockResolvedValue(resposta(200, {}))

    await request('/usuarios/login', { method: 'POST', body: { email: 'a', senha: 'b' }, auth: false })

    const init = fetchMock.mock.calls[0][1]
    expect(init.headers.Authorization).toBeUndefined()
    expect(init.headers['Content-Type']).toBe('application/json')
    expect(init.body).toBe('{"email":"a","senha":"b"}')
  })

  it('devolve undefined em respostas 204', async () => {
    fetchMock.mockResolvedValue(new Response(null, { status: 204 }))

    await expect(request('/jogos/1', { method: 'DELETE' })).resolves.toBeUndefined()
  })

  it('converte o corpo de erro da API em ApiError com fieldErrors', async () => {
    fetchMock.mockResolvedValue(
      resposta(400, { status: 400, message: 'Dados inválidos', fieldErrors: { titulo: 'Título é obrigatório' } }),
    )

    const erro = await request('/jogos', { method: 'POST', body: {} }).catch((e: unknown) => e)

    expect(erro).toBeInstanceOf(ApiError)
    expect((erro as ApiError).status).toBe(400)
    expect((erro as ApiError).message).toBe('Dados inválidos')
    expect((erro as ApiError).fieldErrors).toEqual({ titulo: 'Título é obrigatório' })
  })

  it('avisa a sessão quando um 401 vem de uma chamada autenticada', async () => {
    fetchMock.mockResolvedValue(resposta(401, { status: 401, message: 'Autenticação necessária' }))

    await expect(request('/usuarios')).rejects.toMatchObject({ status: 401 })
    expect(onUnauthorized).toHaveBeenCalledOnce()
  })

  it('não trata como sessão expirada o 401 de um login com senha errada', async () => {
    fetchMock.mockResolvedValue(resposta(401, { status: 401, message: 'Email ou senha inválidos' }))

    await expect(
      request('/usuarios/login', { method: 'POST', body: {}, auth: false }),
    ).rejects.toMatchObject({ message: 'Email ou senha inválidos' })
    expect(onUnauthorized).not.toHaveBeenCalled()
  })

  it('explica quando o servidor está fora do ar', async () => {
    fetchMock.mockRejectedValue(new TypeError('Failed to fetch'))

    const erro = await request('/jogos').catch((e: unknown) => e)

    expect(erro).toBeInstanceOf(ApiError)
    expect((erro as ApiError).status).toBe(0)
    expect((erro as ApiError).message).toContain('backend')
  })

  it('usa uma mensagem genérica quando o erro não vem em JSON', async () => {
    fetchMock.mockResolvedValue(new Response('<html>Bad Gateway</html>', { status: 502 }))

    await expect(request('/jogos')).rejects.toMatchObject({ status: 502, message: expect.stringContaining('502') })
  })
})
