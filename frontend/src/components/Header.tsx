import { useState, type FormEvent } from 'react'
import { Link, NavLink, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { Button } from './Button'
import { inputClass } from './FormField'

function Logo() {
  return (
    <Link to="/" className="text-vapor text-2xl font-black tracking-[0.2em]" aria-label="VAPOR, página inicial">
      VAPOR
    </Link>
  )
}

function BuscaDeJogos() {
  const [params] = useSearchParams()
  const navegar = useNavigate()
  const [texto, setTexto] = useState(params.get('q') ?? '')

  function buscar(evento: FormEvent) {
    evento.preventDefault()
    const termo = texto.trim()
    navegar(termo ? `/?q=${encodeURIComponent(termo)}` : '/')
  }

  return (
    <form onSubmit={buscar} role="search" className="w-full max-w-md flex-1">
      <input
        type="search"
        value={texto}
        onChange={(e) => setTexto(e.target.value)}
        placeholder="Buscar jogos..."
        aria-label="Buscar jogos pelo título"
        className={inputClass}
      />
    </form>
  )
}

export function Header() {
  const { logado, usuario, isAdmin, sair } = useAuth()
  const navegar = useNavigate()

  return (
    <header className="sticky top-0 z-20 border-b border-line bg-bg/90 backdrop-blur">
      <div className="mx-auto flex w-full max-w-7xl flex-wrap items-center gap-x-6 gap-y-3 px-4 py-3">
        <Logo />
        <nav className="flex items-center gap-4 text-sm">
          <NavLink to="/" end className={({ isActive }) => (isActive ? 'text-ink' : 'text-muted hover:text-ink')}>
            Loja
          </NavLink>
          {isAdmin && (
            <NavLink to="/admin" className={({ isActive }) => (isActive ? 'text-ink' : 'text-muted hover:text-ink')}>
              Admin
            </NavLink>
          )}
        </nav>
        <BuscaDeJogos />
        <div className="ml-auto flex items-center gap-2">
          {logado && usuario ? (
            <>
              <span className="hidden text-sm text-muted sm:inline">
                {usuario.nome}
                {isAdmin && <span className="ml-2 rounded bg-violet/20 px-1.5 py-0.5 text-[11px] text-violet">ADMIN</span>}
              </span>
              <Button
                variant="ghost"
                onClick={() => {
                  sair()
                  navegar('/')
                }}
              >
                Sair
              </Button>
            </>
          ) : (
            <>
              <Button variant="ghost" onClick={() => navegar('/login')}>
                Entrar
              </Button>
              <Button onClick={() => navegar('/cadastro')}>Cadastrar</Button>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
