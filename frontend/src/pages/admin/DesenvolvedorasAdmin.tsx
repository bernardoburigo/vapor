import { useState, type FormEvent } from 'react'
import {
  atualizarDesenvolvedora,
  criarDesenvolvedora,
  listarDesenvolvedoras,
  removerDesenvolvedora,
} from '../../api/desenvolvedoras'
import type { Desenvolvedora } from '../../api/types'
import { Alert } from '../../components/Alert'
import { Button } from '../../components/Button'
import { ConfirmButton } from '../../components/ConfirmButton'
import { DataTable } from '../../components/DataTable'
import { FormField, inputClass } from '../../components/FormField'
import { Modal } from '../../components/Modal'
import { useAsync } from '../../lib/useAsync'
import { useSubmit } from '../../lib/useSubmit'

type Edicao = { item: Desenvolvedora | null } | null

export function DesenvolvedorasAdmin() {
  const lista = useAsync(listarDesenvolvedoras, [])
  const [edicao, setEdicao] = useState<Edicao>(null)
  const [erro, setErro] = useState<string | null>(null)

  async function remover(d: Desenvolvedora) {
    setErro(null)
    try {
      await removerDesenvolvedora(d.id)
      lista.reload()
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Não foi possível remover.')
    }
  }

  const itens = lista.data ?? []

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3">
        <p className="text-sm text-muted">{itens.length} desenvolvedoras</p>
        <Button onClick={() => setEdicao({ item: null })}>Nova desenvolvedora</Button>
      </div>

      {erro && <Alert>{erro}</Alert>}
      {lista.error && <Alert>{lista.error.message}</Alert>}

      <DataTable
        cabecalhos={['Nome', 'País', 'Site', 'Ações']}
        quantidade={itens.length}
        vazio="Nenhuma desenvolvedora."
      >
        {itens.map((d) => (
          <tr key={d.id}>
            <td className="px-4 py-3 font-medium">{d.nome}</td>
            <td className="px-4 py-3 text-muted">{d.paisOrigem ?? '—'}</td>
            <td className="px-4 py-3 text-muted">
              {d.site ? (
                <a href={d.site} target="_blank" rel="noreferrer" className="text-cyan hover:underline">
                  {d.site.replace(/^https?:\/\//, '')}
                </a>
              ) : (
                '—'
              )}
            </td>
            <td className="px-4 py-3">
              <div className="flex flex-wrap items-center gap-2">
                <Button variant="ghost" onClick={() => setEdicao({ item: d })}>
                  Editar
                </Button>
                <ConfirmButton rotulo="Remover" pergunta={`Remover “${d.nome}”?`} onConfirm={() => remover(d)} />
              </div>
            </td>
          </tr>
        ))}
      </DataTable>

      <Modal
        aberto={edicao !== null}
        titulo={edicao?.item ? 'Editar desenvolvedora' : 'Nova desenvolvedora'}
        onClose={() => setEdicao(null)}
      >
        {edicao && (
          <DesenvolvedoraForm
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

function DesenvolvedoraForm({
  inicial,
  onSalvo,
  onCancelar,
}: {
  inicial: Desenvolvedora | null
  onSalvo: () => void
  onCancelar: () => void
}) {
  const [nome, setNome] = useState(inicial?.nome ?? '')
  const [pais, setPais] = useState(inicial?.paisOrigem ?? '')
  const [site, setSite] = useState(inicial?.site ?? '')
  const { erro, campos, enviando, executar } = useSubmit()

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    const dados = { nome, paisOrigem: pais.trim() || null, site: site.trim() || null }
    const ok = await executar(() =>
      inicial ? atualizarDesenvolvedora(inicial.id, dados) : criarDesenvolvedora(dados),
    )
    if (ok) onSalvo()
  }

  return (
    <form onSubmit={enviar} className="space-y-4" noValidate>
      <FormField label="Nome" htmlFor="dev-nome" erro={campos?.nome}>
        <input
          id="dev-nome"
          autoFocus
          value={nome}
          onChange={(e) => setNome(e.target.value)}
          disabled={enviando}
          className={inputClass}
        />
      </FormField>
      <FormField label="País de origem" htmlFor="dev-pais" erro={campos?.paisOrigem}>
        <input
          id="dev-pais"
          value={pais}
          onChange={(e) => setPais(e.target.value)}
          disabled={enviando}
          className={inputClass}
        />
      </FormField>
      <FormField label="Site" htmlFor="dev-site" erro={campos?.site} dica="Começa com http:// ou https://">
        <input
          id="dev-site"
          value={site}
          onChange={(e) => setSite(e.target.value)}
          disabled={enviando}
          placeholder="https://exemplo.com"
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
