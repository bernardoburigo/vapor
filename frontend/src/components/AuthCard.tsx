import type { ReactNode } from 'react'

interface AuthCardProps {
  titulo: string
  subtitulo: string
  children: ReactNode
  rodape: ReactNode
}

export function AuthCard({ titulo, subtitulo, children, rodape }: AuthCardProps) {
  return (
    <div className="mx-auto mt-6 w-full max-w-md rounded-2xl border border-line bg-panel p-8 shadow-xl">
      <p className="text-vapor text-sm font-black tracking-[0.3em]">VAPOR</p>
      <h1 className="mt-3 text-2xl font-bold">{titulo}</h1>
      <p className="mt-1 text-sm text-muted">{subtitulo}</p>
      <div className="mt-6">{children}</div>
      <p className="mt-6 border-t border-line pt-4 text-center text-sm text-muted">{rodape}</p>
    </div>
  )
}
