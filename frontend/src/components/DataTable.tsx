import type { ReactNode } from 'react'

interface DataTableProps {
  cabecalhos: string[]
  children: ReactNode
  /** Texto exibido quando não há linhas. */
  vazio?: string
  quantidade: number
}

export function DataTable({ cabecalhos, children, vazio = 'Nada cadastrado ainda.', quantidade }: DataTableProps) {
  return (
    <div className="overflow-x-auto rounded-xl border border-line">
      <table className="w-full min-w-[640px] text-left text-sm">
        <thead className="bg-panel-2 text-xs uppercase tracking-wide text-muted">
          <tr>
            {cabecalhos.map((c) => (
              <th key={c} scope="col" className="px-4 py-3 font-medium">
                {c}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-line bg-panel">
          {quantidade === 0 ? (
            <tr>
              <td colSpan={cabecalhos.length} className="px-4 py-8 text-center text-muted">
                {vazio}
              </td>
            </tr>
          ) : (
            children
          )}
        </tbody>
      </table>
    </div>
  )
}
