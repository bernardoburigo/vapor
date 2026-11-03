#!/usr/bin/env node
// Popula a API do VAPOR com dados de demonstração. Pode ser executado várias vezes:
// só cria o que ainda não existe. Uso: `npm run seed` (com o backend no ar).
//
// Variáveis de ambiente (todas opcionais):
//   API_URL            URL base da API            (padrão: http://localhost:8081/api)
//   SEED_ADMIN_EMAIL   e-mail do ADMIN            (padrão: admin@vapor.com)
//   SEED_ADMIN_SENHA   senha do ADMIN             (padrão: admin123)

import { avaliacoes, categorias, desenvolvedoras, jogos, SENHA_DEMO, usuarios } from './seed-data.mjs'

const API = (process.env.API_URL ?? 'http://localhost:8081/api').replace(/\/+$/, '')
const ADMIN_EMAIL = process.env.SEED_ADMIN_EMAIL ?? 'admin@vapor.com'
const ADMIN_SENHA = process.env.SEED_ADMIN_SENHA ?? 'admin123'

class SeedError extends Error {}

async function http(metodo, caminho, { token, corpo } = {}) {
  const headers = { Accept: 'application/json' }
  if (corpo !== undefined) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  let resposta
  try {
    resposta = await fetch(`${API}${caminho}`, {
      method: metodo,
      headers,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    })
  } catch {
    throw new SeedError(
      `Não foi possível conectar em ${API}.\nSuba o backend antes (docker compose up -d && ./mvnw spring-boot:run) ou ajuste API_URL.`,
    )
  }
  const texto = await resposta.text()
  let dados = null
  try {
    dados = texto ? JSON.parse(texto) : null
  } catch {
    dados = texto
  }
  return { status: resposta.status, dados }
}

/** Exige um dos status esperados; qualquer outro é uma falha real (e interrompe o seed). */
function esperar(resposta, aceitos, contexto) {
  if (!aceitos.includes(resposta.status)) {
    const detalhe = resposta.dados?.message ?? JSON.stringify(resposta.dados)
    throw new SeedError(`${contexto}: a API respondeu ${resposta.status} (${detalhe})`)
  }
  return resposta
}

const placar = {}
function contar(grupo, criado) {
  placar[grupo] ??= { criados: 0, existentes: 0 }
  placar[grupo][criado ? 'criados' : 'existentes']++
}

async function entrar(email, senha, contexto) {
  const r = await http('POST', '/usuarios/login', { corpo: { email, senha } })
  if (r.status === 401) {
    throw new SeedError(`${contexto}: e-mail ou senha incorretos (${email}).`)
  }
  return esperar(r, [200], contexto).dados.token
}

async function semearCategorias(token) {
  const atuais = esperar(await http('GET', '/categorias'), [200], 'Listar categorias').dados
  const ids = new Map(atuais.map((c) => [c.nome.toLowerCase(), c.id]))
  for (const categoria of categorias) {
    const chave = categoria.nome.toLowerCase()
    if (ids.has(chave)) {
      contar('categorias', false)
      continue
    }
    const r = esperar(await http('POST', '/categorias', { token, corpo: categoria }), [201], `Criar categoria ${categoria.nome}`)
    ids.set(chave, r.dados.id)
    contar('categorias', true)
  }
  return ids
}

async function semearDesenvolvedoras(token) {
  const atuais = esperar(await http('GET', '/desenvolvedoras'), [200], 'Listar desenvolvedoras').dados
  const ids = new Map(atuais.map((d) => [d.nome.toLowerCase(), d.id]))
  for (const dev of desenvolvedoras) {
    const chave = dev.nome.toLowerCase()
    if (ids.has(chave)) {
      contar('desenvolvedoras', false)
      continue
    }
    const r = esperar(await http('POST', '/desenvolvedoras', { token, corpo: dev }), [201], `Criar desenvolvedora ${dev.nome}`)
    ids.set(chave, r.dados.id)
    contar('desenvolvedoras', true)
  }
  return ids
}

async function listarTodosOsJogos() {
  const todos = []
  for (let pagina = 0; ; pagina++) {
    const r = esperar(await http('GET', `/jogos?size=100&page=${pagina}`), [200], 'Listar jogos').dados
    todos.push(...r.content)
    if (pagina + 1 >= r.page.totalPages) return todos
  }
}

async function semearJogos(token, idsCategorias, idsDevs) {
  const ids = new Map((await listarTodosOsJogos()).map((j) => [j.titulo.toLowerCase(), j.id]))
  for (const jogo of jogos) {
    const chave = jogo.titulo.toLowerCase()
    if (ids.has(chave)) {
      contar('jogos', false)
      continue
    }
    const desenvolvedoraId = idsDevs.get(jogo.desenvolvedora.toLowerCase())
    const categoriaIds = jogo.categorias.map((c) => idsCategorias.get(c.toLowerCase()))
    if (!desenvolvedoraId || categoriaIds.some((id) => !id)) {
      throw new SeedError(`Dados inconsistentes para "${jogo.titulo}": desenvolvedora ou categoria desconhecida.`)
    }
    const corpo = {
      titulo: jogo.titulo,
      descricao: jogo.descricao,
      preco: jogo.preco,
      dataLancamento: jogo.dataLancamento,
      desenvolvedoraId,
      categoriaIds,
    }
    const r = esperar(await http('POST', '/jogos', { token, corpo }), [201], `Criar jogo ${jogo.titulo}`)
    ids.set(chave, r.dados.id)
    contar('jogos', true)
  }
  return ids
}

async function semearUsuarios() {
  const tokens = new Map()
  for (const usuario of usuarios) {
    const r = await http('POST', '/usuarios', { corpo: { ...usuario, senha: SENHA_DEMO } })
    esperar(r, [201, 409], `Criar usuário ${usuario.email}`)
    contar('usuários demo', r.status === 201)
    tokens.set(usuario.email, await entrar(usuario.email, SENHA_DEMO, `Login de ${usuario.email}`))
  }
  return tokens
}

async function semearAvaliacoes(tokens, idsJogos) {
  for (const [email, titulo, nota, comentario] of avaliacoes) {
    const jogoId = idsJogos.get(titulo.toLowerCase())
    if (!jogoId) throw new SeedError(`Avaliação para jogo desconhecido: "${titulo}".`)
    const r = await http('POST', '/avaliacoes', { token: tokens.get(email), corpo: { jogoId, nota, comentario } })
    esperar(r, [201, 409], `Avaliar "${titulo}" como ${email}`)
    contar('avaliações', r.status === 201)
  }
}

async function main() {
  console.log(`VAPOR seed → ${API}\n`)

  // Confere se a API está no ar antes de qualquer coisa (mensagem clara se não estiver).
  esperar(await http('GET', '/categorias'), [200], 'Verificar a API')

  const token = await entrar(ADMIN_EMAIL, ADMIN_SENHA, 'Login do ADMIN (defina SEED_ADMIN_EMAIL e SEED_ADMIN_SENHA se mudou as credenciais)')
  const idsCategorias = await semearCategorias(token)
  const idsDevs = await semearDesenvolvedoras(token)
  const idsJogos = await semearJogos(token, idsCategorias, idsDevs)
  const tokens = await semearUsuarios()
  await semearAvaliacoes(tokens, idsJogos)

  console.log('Resumo (criados / já existiam):')
  for (const [grupo, { criados, existentes }] of Object.entries(placar)) {
    console.log(`  ${grupo.padEnd(16)} ${String(criados).padStart(3)} / ${existentes}`)
  }
  console.log(`\nLogins de demonstração (senha ${SENHA_DEMO}):`)
  for (const u of usuarios) console.log(`  ${u.email}`)
  console.log(`\nADMIN: ${ADMIN_EMAIL}`)
  console.log('\nPreços e datas são aproximados e servem apenas para a demonstração.')
}

main().catch((erro) => {
  console.error(erro instanceof SeedError ? `\n✖ ${erro.message}` : erro)
  process.exit(1)
})
