import { useState, type FormEvent } from 'react'
import { atualizarCategoria, criarCategoria, listarCategorias, removerCategoria } from '../../api/categorias'
import type { Categoria } from '../../api/types'
import { Alert } from '../../components/Alert'
import { Button } from '../../components/Button'
import { ConfirmButton } from '../../components/ConfirmButton'
import { DataTable } from '../../components/DataTable'
import { FormField, inputClass } from '../../components/FormField'
import { Modal } from '../../components/Modal'
import { useAsync } from '../../lib/useAsync'
import { useSubmit } from '../../lib/useSubmit'

type Edicao = { item: Categoria | null } | null

export function CategoriasAdmin() {
  const lista = useAsync(listarCategorias, [])
  const [edicao, setEdicao] = useState<Edicao>(null)
  const [erro, setErro] = useState<string | null>(null)

  async function remover(c: Categoria) {
    setErro(null)
    try {
      await removerCategoria(c.id)
      lista.reload()
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Não foi possível remover.')
    }
  }

  const itens = lista.data ?? []

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3">
        <p className="text-sm text-muted">{itens.length} categorias</p>
        <Button onClick={() => setEdicao({ item: null })}>Nova categoria</Button>
      </div>

      {erro && <Alert>{erro}</Alert>}
      {lista.error && <Alert>{lista.error.message}</Alert>}

      <DataTable cabecalhos={['Nome', 'Descrição', 'Ações']} quantidade={itens.length} vazio="Nenhuma categoria.">
        {itens.map((c) => (
          <tr key={c.id}>
            <td className="px-4 py-3 font-medium">{c.nome}</td>
            <td className="px-4 py-3 text-muted">{c.descricao ?? '—'}</td>
            <td className="px-4 py-3">
              <div className="flex flex-wrap items-center gap-2">
                <Button variant="ghost" onClick={() => setEdicao({ item: c })}>
                  Editar
                </Button>
                <ConfirmButton rotulo="Remover" pergunta={`Remover “${c.nome}”?`} onConfirm={() => remover(c)} />
              </div>
            </td>
          </tr>
        ))}
      </DataTable>

      <Modal
        aberto={edicao !== null}
        titulo={edicao?.item ? 'Editar categoria' : 'Nova categoria'}
        onClose={() => setEdicao(null)}
      >
        {edicao && (
          <CategoriaForm
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

function CategoriaForm({
  inicial,
  onSalvo,
  onCancelar,
}: {
  inicial: Categoria | null
  onSalvo: () => void
  onCancelar: () => void
}) {
  const [nome, setNome] = useState(inicial?.nome ?? '')
  const [descricao, setDescricao] = useState(inicial?.descricao ?? '')
  const { erro, campos, enviando, executar } = useSubmit()

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    const dados = { nome, descricao: descricao.trim() || null }
    const ok = await executar(() => (inicial ? atualizarCategoria(inicial.id, dados) : criarCategoria(dados)))
    if (ok) onSalvo()
  }

  return (
    <form onSubmit={enviar} className="space-y-4" noValidate>
      <FormField label="Nome" htmlFor="cat-nome" erro={campos?.nome}>
        <input
          id="cat-nome"
          autoFocus
          value={nome}
          onChange={(e) => setNome(e.target.value)}
          disabled={enviando}
          className={inputClass}
        />
      </FormField>
      <FormField label="Descrição" htmlFor="cat-descricao" erro={campos?.descricao}>
        <input
          id="cat-descricao"
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
          disabled={enviando}
          className={inputClass}
        />
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
