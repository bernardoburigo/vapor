import { Link, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { Alert } from './Alert'
import { Header } from './Header'

export function Layout() {
  const { aviso, limparAviso } = useAuth()

  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      {aviso && (
        <div className="mx-auto w-full max-w-7xl px-4 pt-4">
          <Alert tone="info">
            <span className="flex flex-wrap items-center gap-x-4 gap-y-1">
              {aviso}
              <Link to="/login" className="font-semibold underline">
                Entrar
              </Link>
              <button type="button" onClick={limparAviso} className="underline">
                Dispensar
              </button>
            </span>
          </Alert>
        </div>
      )}
      <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-6">
        <Outlet />
      </main>
      <footer className="border-t border-line py-6 text-center text-xs text-muted">
        VAPOR · projeto de Desenvolvimento Backend com Spring Boot · UNESC
      </footer>
    </div>
  )
}
