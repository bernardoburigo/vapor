import type { ReactNode } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'

/**
 * Esconde a tela de administração de quem não é ADMIN. É só conveniência de interface:
 * quem de fato barra as operações é o backend (403 para USER).
 */
export function RequireAdmin({ children }: { children: ReactNode }) {
  const { logado, isAdmin } = useAuth()
  const local = useLocation()

  if (!logado) return <Navigate to="/login" replace state={{ from: local.pathname }} />

  if (!isAdmin) {
    return (
      <div className="mx-auto mt-16 max-w-md rounded-2xl border border-line bg-panel p-8 text-center">
        <h1 className="text-xl font-semibold">Acesso restrito</h1>
        <p className="mt-2 text-muted">Esta área é exclusiva de administradores.</p>
        <Link to="/" className="mt-6 inline-block text-cyan hover:underline">
          Voltar à loja
        </Link>
      </div>
    )
  }

  return <>{children}</>
}
