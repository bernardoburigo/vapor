import { useEffect, useState, type FormEvent } from 'react'
import { listarCategorias } from '../../api/categorias'
import { listarDesenvolvedoras } from '../../api/desenvolvedoras'
import { atualizarJogo, criarJogo, listarJogos, removerJogo } from '../../api/jogos'
import type { Jogo } from '../../api/types'
import { Alert } from '../../components/Alert'
import { Button } from '../../components/Button'
import { ConfirmButton } from '../../components/ConfirmButton'
import { DataTable } from '../../components/DataTable'
import { FormField, inputClass } from '../../components/FormField'
import { Modal } from '../../components/Modal'
import { Pagination } from '../../components/Pagination'
import { Stars } from '../../components/Stars'
import { formatPrice, formatRating, pluralize } from '../../lib/format'
import { useAsync } from '../../lib/useAsync'
import { useSubmit } from '../../lib/useSubmit'

const POR_PAGINA = 10

type Edicao = { item: Jogo | null } | null

export function JogosAdmin() {
  const [busca, setBusca] = useState('')
  const [termo, setTermo] = useState('')
  const [pagina, setPagina] = useState(1)
  const [edicao, setEdicao] = useState<Edicao>(null)
  const [erro, setErro] = useState<string | null>(null)

  // Espera o usuário parar de digitar antes de consultar a API.
  useEffect(() => {
    const timer = window.setTimeout(() => {
      setTermo(busca.trim())
      setPagina(1)
    }, 300)
    return () => window.clearTimeout(timer)
  }, [busca])

  const lista = useAsync(
    // Mais recentes primeiro: o jogo que o ADMIN acabou de criar aparece logo na primeira página.
    () => listarJogos({ titulo: termo || undefined, page: pagina - 1, size: POR_PAGINA, sort: 'id,desc' }),
    [termo, pagina],
  )

  async function remover(jogo: Jogo) {
    setErro(null)
    try {
      await removerJogo(jogo.id)
      lista.reload()
    } catch (e) {
      // Ex.: 409 "Não é possível remover: o jogo possui avaliações".
      setErro(e instanceof Error ? e.message : 'Não foi possível remover.')
    }
  }

  const itens = lista.data?.content ?? []

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-1 items-center gap-3">
          <input
            type="search"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Buscar por título..."
            aria-label="Filtrar a tabela de jogos pelo título"
            className={`${inputClass} max-w-xs`}
          />
          {lista.data && (
            <span className="text-sm text-muted">
              {pluralize(lista.data.page.totalElements, 'jogo', 'jogos')} · mais recentes primeiro
            </span>
          )}
        </div>
        <Button onClick={() => setEdicao({ item: null })}>Novo jogo</Button>
      </div>

      {erro && <Alert>{erro}</Alert>}
      {lista.error && <Alert>{lista.error.message}</Alert>}

      <DataTable
        cabecalhos={['Título', 'Desenvolvedora', 'Categorias', 'Preço', 'Nota', 'Ações']}
        quantidade={itens.length}
        vazio="Nenhum jogo encontrado."
      >
        {itens.map((j) => (
          <tr key={j.id}>
            <td className="px-4 py-3 font-medium">{j.titulo}</td>
            <td className="px-4 py-3 text-muted">{j.desenvolvedora.nome}</td>
            <td className="px-4 py-3 text-muted">{j.categorias.map((c) => c.nome).join(', ') || '—'}</td>
            <td className="whitespace-nowrap px-4 py-3">{formatPrice(j.preco)}</td>
            <td className="whitespace-nowrap px-4 py-3">
              <span className="flex items-center gap-1.5">
                <Stars value={j.notaMedia} className="h-3.5" />
                <span className="text-xs text-muted">
                  {formatRating(j.notaMedia)} ({j.totalAvaliacoes})
                </span>
              </span>
            </td>
            <td className="px-4 py-3">
              <div className="flex flex-wrap items-center gap-2">
                <Button variant="ghost" onClick={() => setEdicao({ item: j })}>
                  Editar
                </Button>
                <ConfirmButton rotulo="Remover" pergunta={`Remover “${j.titulo}”?`} onConfirm={() => remover(j)} />
              </div>
            </td>
          </tr>
        ))}
      </DataTable>

      <Pagination pagina={pagina} totalPaginas={lista.data?.page.totalPages ?? 1} onChange={setPagina} />

      <Modal aberto={edicao !== null} titulo={edicao?.item ? 'Editar jogo' : 'Novo jogo'} onClose={() => setEdicao(null)}>
        {edicao && (
          <JogoForm
            inicial={edicao.item}
            onCancelar={() => setEdicao(null)}
            onSalvo={() => {
              setEdicao(null)
              lista.reload()
            }}
          />
        )}
      </Modal>
    </div>
  )
}

function JogoForm({
  inicial,
  onSalvo,
  onCancelar,
}: {
  inicial: Jogo | null
  onSalvo: () => void
  onCancelar: () => void
}) {
  const desenvolvedoras = useAsync(listarDesenvolvedoras, [])
  const categorias = useAsync(listarCategorias, [])

  const [titulo, setTitulo] = useState(inicial?.titulo ?? '')
  const [descricao, setDescricao] = useState(inicial?.descricao ?? '')
  const [preco, setPreco] = useState(inicial ? String(inicial.preco) : '')
  const [data, setData] = useState(inicial?.dataLancamento ?? '')
  const [desenvolvedoraId, setDesenvolvedoraId] = useState(inicial ? String(inicial.desenvolvedora.id) : '')
  const [categoriaIds, setCategoriaIds] = useState<number[]>(inicial?.categorias.map((c) => c.id) ?? [])
  const { erro, campos, enviando, executar } = useSubmit()

  function alternarCategoria(id: number) {
    setCategoriaIds((atuais) => (atuais.includes(id) ? atuais.filter((c) => c !== id) : [...atuais, id]))
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    const valorPreco = preco.trim() === '' ? null : Number(preco.replace(',', '.'))
    const dados = {
      titulo,
      descricao: descricao.trim() || null,
      preco: valorPreco !== null && Number.isNaN(valorPreco) ? null : valorPreco,
      dataLancamento: data || null,
      desenvolvedoraId: desenvolvedoraId ? Number(desenvolvedoraId) : null,
      categoriaIds,
    }
    const ok = await executar(() => (inicial ? atualizarJogo(inicial.id, dados) : criarJogo(dados)))
    if (ok) onSalvo()
  }

  return (
    <form onSubmit={enviar} className="space-y-4" noValidate>
      <FormField label="Título" htmlFor="jogo-titulo" erro={campos?.titulo}>
        <input
          id="jogo-titulo"
          autoFocus
          value={titulo}
          onChange={(e) => setTitulo(e.target.value)}
          disabled={enviando}
          className={inputClass}
        />
      </FormField>

      <FormField label="Descrição" htmlFor="jogo-descricao" erro={campos?.descricao}>
        <textarea
          id="jogo-descricao"
          rows={3}
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
          disabled={enviando}
          className={inputClass}
        />
      </FormField>

      <div className="grid gap-4 sm:grid-cols-2">
        <FormField label="Preço (R$)" htmlFor="jogo-preco" erro={campos?.preco} dica="Use 0 para jogos gratuitos.">
          <input
            id="jogo-preco"
            inputMode="decimal"
            value={preco}
            onChange={(e) => setPreco(e.target.value)}
            disabled={enviando}
            placeholder="59,90"
            className={inputClass}
          />
        </FormField>
        <FormField label="Data de lançamento" htmlFor="jogo-data" erro={campos?.dataLancamento}>
          <input
            id="jogo-data"
            type="date"
            value={data}
            onChange={(e) => setData(e.target.value)}
            disabled={enviando}
            className={inputClass}
          />
        </FormField>
      </div>

      <FormField label="Desenvolvedora" htmlFor="jogo-dev" erro={campos?.desenvolvedoraId}>
        <select
          id="jogo-dev"
          value={desenvolvedoraId}
          onChange={(e) => setDesenvolvedoraId(e.target.value)}
          disabled={enviando}
          className={inputClass}
        >
          <option value="">Selecione...</option>
          {desenvolvedoras.data?.map((d) => (
            <option key={d.id} value={d.id}>
              {d.nome}
            </option>
          ))}
        </select>
      </FormField>

      <FormField label="Categorias" htmlFor="jogo-categorias" erro={campos?.categoriaIds} dica="Escolha ao menos uma.">
        <div id="jogo-categorias" role="group" className="flex flex-wrap gap-2">
          {categorias.data?.map((c) => {
            const marcada = categoriaIds.includes(c.id)
            return (
              <label
                key={c.id}
                className={`cursor-pointer rounded-full border px-3 py-1 text-sm transition ${
                  marcada ? 'border-cyan bg-cyan/15 text-ink' : 'border-line text-muted hover:border-cyan/60'
                }`}
              >
                <input
                  type="checkbox"
                  className="sr-only"
                  checked={marcada}
                  onChange={() => alternarCategoria(c.id)}
                  disabled={enviando}
                />
                {c.nome}
              </label>
            )
          })}
          {categorias.data?.length === 0 && <span className="text-sm text-muted">Cadastre uma categoria primeiro.</span>}
        </div>
      </FormField>

      {erro && !campos && <Alert>{erro.message}</Alert>}

      <div className="flex justify-end gap-2">
        <Button variant="ghost" onClick={onCancelar} disabled={enviando}>
          Cancelar
        </Button>
        <Button type="submit" disabled={enviando}>
          {enviando ? 'Salvando...' : 'Salvar'}
        </Button>
      </div>
    </form>
  )
}
