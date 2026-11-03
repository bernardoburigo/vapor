// Formatos de entrada e saída da API do backend (ver os DTOs em src/main/java/.../dto).

export type Role = 'USER' | 'ADMIN'

export interface Usuario {
  id: number
  nome: string
  email: string
  role: Role
  dataCadastro: string
}

export interface AuthResponse {
  token: string
  tipo: string
  expiraEmSegundos: number
  usuario: Usuario
}

export interface Categoria {
  id: number
  nome: string
  descricao: string | null
}

export interface Desenvolvedora {
  id: number
  nome: string
  paisOrigem: string | null
  site: string | null
}

export interface Jogo {
  id: number
  titulo: string
  descricao: string | null
  preco: number
  dataLancamento: string | null
  desenvolvedora: { id: number; nome: string }
  categorias: Categoria[]
  /** Média de 1 a 5 com uma casa decimal; nulo enquanto o jogo não tem avaliações. */
  notaMedia: number | null
  totalAvaliacoes: number
}

export interface Avaliacao {
  id: number
  usuarioId: number
  usuarioNome: string
  jogoId: number
  jogoTitulo: string
  nota: number
  comentario: string | null
  dataAvaliacao: string
}

export interface PageResponse<T> {
  content: T[]
  page: { size: number; number: number; totalElements: number; totalPages: number }
}

export interface JogoInput {
  titulo: string
  descricao: string | null
  /** Nulo quando o campo está vazio no formulário: o backend responde "Preço é obrigatório". */
  preco: number | null
  dataLancamento: string | null
  desenvolvedoraId: number | null
  categoriaIds: number[]
}

export interface CategoriaInput {
  nome: string
  descricao: string | null
}

export interface DesenvolvedoraInput {
  nome: string
  paisOrigem: string | null
  site: string | null
}

export interface RegistroInput {
  nome: string
  email: string
  senha: string
}

/** Parâmetros aceitos por GET /jogos. */
export interface JogoFiltro {
  categoriaId?: number
  desenvolvedoraId?: number
  titulo?: string
  page?: number
  size?: number
  sort?: string
}
