import type { ReactNode } from 'react'

type Tom = 'error' | 'info' | 'ok'

const TONS: Record<Tom, string> = {
  error: 'border-danger/40 bg-danger/10 text-danger',
  info: 'border-cyan/40 bg-cyan/10 text-cyan',
  ok: 'border-ok/40 bg-ok/10 text-ok',
}

export function Alert({ tone = 'error', children }: { tone?: Tom; children: ReactNode }) {
  return (
    <div role={tone === 'error' ? 'alert' : 'status'} className={`rounded-lg border px-4 py-3 text-sm ${TONS[tone]}`}>
      {children}
    </div>
  )
}
