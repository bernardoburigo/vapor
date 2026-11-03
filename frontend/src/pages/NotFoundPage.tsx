import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <div className="mx-auto mt-16 max-w-md text-center">
      <p className="text-vapor text-6xl font-black">404</p>
      <h1 className="mt-2 text-xl font-semibold">Página não encontrada</h1>
      <p className="mt-2 text-muted">O endereço que você tentou abrir não existe.</p>
      <Link to="/" className="mt-6 inline-block text-cyan hover:underline">
        Voltar à loja
      </Link>
    </div>
  )
}
