import { useSearchParams } from 'react-router-dom'
import { CategoriasAdmin } from './CategoriasAdmin'
import { DesenvolvedorasAdmin } from './DesenvolvedorasAdmin'
import { JogosAdmin } from './JogosAdmin'

const ABAS = [
  { id: 'jogos', rotulo: 'Jogos' },
  { id: 'categorias', rotulo: 'Categorias' },
  { id: 'desenvolvedoras', rotulo: 'Desenvolvedoras' },
] as const

type AbaId = (typeof ABAS)[number]['id']

export function AdminPage() {
  const [params, setParams] = useSearchParams()
  const pedida = params.get('aba')
  const aba: AbaId = ABAS.some((a) => a.id === pedida) ? (pedida as AbaId) : 'jogos'

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Painel administrativo</h1>
        <p className="text-sm text-muted">Gerencie o catálogo da loja. Disponível apenas para administradores.</p>
      </div>

      <div role="tablist" aria-label="Seções do painel" className="flex gap-1 border-b border-line">
        {ABAS.map((a) => (
          <button
            key={a.id}
            type="button"
            role="tab"
            id={`aba-${a.id}`}
            aria-selected={aba === a.id}
            aria-controls={`painel-${a.id}`}
            onClick={() => setParams(a.id === 'jogos' ? {} : { aba: a.id })}
            className={`-mb-px border-b-2 px-4 py-2 text-sm font-medium transition ${
              aba === a.id ? 'border-cyan text-ink' : 'border-transparent text-muted hover:text-ink'
            }`}
          >
            {a.rotulo}
          </button>
        ))}
      </div>

      <div role="tabpanel" id={`painel-${aba}`} aria-labelledby={`aba-${aba}`}>
        {aba === 'jogos' && <JogosAdmin />}
        {aba === 'categorias' && <CategoriasAdmin />}
        {aba === 'desenvolvedoras' && <DesenvolvedorasAdmin />}
      </div>
    </div>
  )
}
