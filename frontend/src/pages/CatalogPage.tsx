import { useSearchParams } from 'react-router-dom'
import { listarCategorias } from '../api/categorias'
import { listarDesenvolvedoras } from '../api/desenvolvedoras'
import { listarJogos } from '../api/jogos'
import { Alert } from '../components/Alert'
import { Button } from '../components/Button'
import { Destaque } from '../components/Destaque'
import { inputBase } from '../components/FormField'
import { GameCard, GameCardSkeleton } from '../components/GameCard'
import { Pagination } from '../components/Pagination'
import { escolherDestaque } from '../lib/destaque'
import { pluralize } from '../lib/format'
import { useAsync } from '../lib/useAsync'

const ORDENACOES = [
  ['titulo,asc', 'Título (A–Z)'],
  ['preco,asc', 'Menor preço'],
  ['preco,desc', 'Maior preço'],
  ['dataLancamento,desc', 'Lançamentos'],
] as const

const POR_PAGINA = 12

function numero(valor: string | null): number | undefined {
  const n = Number(valor)
  return valor && Number.isInteger(n) && n > 0 ? n : undefined
}

export function CatalogPage() {
  const [params, setParams] = useSearchParams()

  // Todo o estado da vitrine vive na URL: o botão Voltar e o link compartilhado funcionam.
  const q = params.get('q')?.trim() ?? ''
  const categoriaId = numero(params.get('categoriaId'))
  const desenvolvedoraId = numero(params.get('desenvolvedoraId'))
  const sortParam = params.get('sort')
  const sort = ORDENACOES.some(([valor]) => valor === sortParam) ? (sortParam as string) : 'titulo,asc'
  const pagina = numero(params.get('page')) ?? 1
  const semFiltros = !q && !categoriaId && !desenvolvedoraId

  function mudar(mudancas: Record<string, string | null>, manterPagina = false) {
    const novo = new URLSearchParams(params)
    for (const [chave, valor] of Object.entries(mudancas)) {
      if (valor === null || valor === '') novo.delete(chave)
      else novo.set(chave, valor)
    }
    if (!manterPagina) novo.delete('page')
    setParams(novo)
  }

  const categorias = useAsync(listarCategorias, [])
  const desenvolvedoras = useAsync(listarDesenvolvedoras, [])
  const destaque = useAsync(() => listarJogos({ size: 100 }).then((r) => escolherDestaque(r.content)), [])
  const jogos = useAsync(
    () =>
      listarJogos({
        categoriaId,
        desenvolvedoraId,
        titulo: q || undefined,
        page: pagina - 1,
        size: POR_PAGINA,
        sort,
      }),
    [categoriaId, desenvolvedoraId, q, pagina, sort],
  )

  const resultado = jogos.data
  const mostrarDestaque = semFiltros && pagina === 1 && destaque.data

  return (
    <div className="space-y-8">
      {mostrarDestaque && <Destaque jogo={destaque.data!} />}

      <section aria-label="Catálogo" className="space-y-5">
        <div className="flex flex-wrap items-end justify-between gap-3">
          <div>
            <h1 className="text-2xl font-bold">Catálogo</h1>
            {resultado && (
              <p className="text-sm text-muted">{pluralize(resultado.page.totalElements, 'jogo', 'jogos')}</p>
            )}
          </div>
          <div className="flex flex-wrap gap-3">
            <label className="sr-only" htmlFor="filtro-desenvolvedora">
              Desenvolvedora
            </label>
            <select
              id="filtro-desenvolvedora"
              className={inputBase}
              value={desenvolvedoraId ?? ''}
              onChange={(e) => mudar({ desenvolvedoraId: e.target.value })}
            >
              <option value="">Todas as desenvolvedoras</option>
              {desenvolvedoras.data?.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.nome}
                </option>
              ))}
            </select>
            <label className="sr-only" htmlFor="ordenacao">
              Ordenar por
            </label>
            <select
              id="ordenacao"
              className={inputBase}
              value={sort}
              onChange={(e) => mudar({ sort: e.target.value === 'titulo,asc' ? null : e.target.value })}
            >
              {ORDENACOES.map(([valor, rotulo]) => (
                <option key={valor} value={valor}>
                  {rotulo}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2" role="group" aria-label="Filtrar por categoria">
          <Chip ativo={!categoriaId} onClick={() => mudar({ categoriaId: null })}>
            Todas
          </Chip>
          {categorias.data?.map((c) => (
            <Chip key={c.id} ativo={categoriaId === c.id} onClick={() => mudar({ categoriaId: String(c.id) })}>
              {c.nome}
            </Chip>
          ))}
          {q && (
            <button
              type="button"
              onClick={() => mudar({ q: null })}
              className="ml-2 rounded-full border border-cyan/50 px-3 py-1 text-sm text-cyan hover:bg-cyan/10"
            >
              Busca: “{q}” ✕
            </button>
          )}
        </div>

        {jogos.error && !resultado ? (
          <div className="space-y-3">
            <Alert>{jogos.error.message}</Alert>
            <Button variant="ghost" onClick={jogos.reload}>
              Tentar de novo
            </Button>
          </div>
        ) : !resultado ? (
          <Grade>
            {Array.from({ length: 8 }, (_, i) => (
              <GameCardSkeleton key={i} />
            ))}
          </Grade>
        ) : resultado.content.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-line p-10 text-center">
            <p className="text-lg font-semibold">Nenhum jogo encontrado</p>
            <p className="mt-1 text-sm text-muted">Tente outros filtros ou busque por outro título.</p>
            {!semFiltros && (
              <Button variant="ghost" className="mt-4" onClick={() => setParams(new URLSearchParams())}>
                Limpar filtros
              </Button>
            )}
          </div>
        ) : (
          <>
            <div className={jogos.loading ? 'opacity-60 transition-opacity' : 'transition-opacity'}>
              <Grade>
                {resultado.content.map((jogo) => (
                  <GameCard key={jogo.id} jogo={jogo} />
                ))}
              </Grade>
            </div>
            <Pagination
              pagina={pagina}
              totalPaginas={resultado.page.totalPages}
              onChange={(nova) => {
                mudar({ page: nova === 1 ? null : String(nova) }, true)
                window.scrollTo({ top: 0, behavior: 'smooth' })
              }}
            />
          </>
        )}
      </section>
    </div>
  )
}

function Grade({ children }: { children: React.ReactNode }) {
  return <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">{children}</div>
}

function Chip({ ativo, onClick, children }: { ativo: boolean; onClick: () => void; children: React.ReactNode }) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-pressed={ativo}
      className={`rounded-full px-3.5 py-1 text-sm transition ${
        ativo ? 'bg-vapor font-semibold text-bg' : 'border border-line text-muted hover:border-cyan/60 hover:text-ink'
      }`}
    >
      {children}
    </button>
  )
}
