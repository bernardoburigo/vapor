# VAPOR — Frontend

Loja de jogos em React que consome a API do backend VAPOR. Foi feita para a apresentação do projeto: poucas telas, todas ligadas a algo que o backend faz (catálogo com filtros, nota média, avaliações, login com JWT e permissões por perfil).

Stack: React 19, TypeScript, Vite 7, Tailwind CSS 4 e React Router 7. Não há biblioteca de estado nem de componentes: são só `fetch`, hooks e Tailwind.

## Pré-requisitos

- Node.js **20.19 ou superior** (ou 22.12+)
- Backend no ar em `http://localhost:8081` (veja o [README da raiz](../README.md#início-rápido))

## Como rodar

```bash
cd frontend
npm install
npm run seed      # popula o catálogo de demonstração (uma vez; pode repetir sem duplicar)
npm run dev       # http://localhost:5173
```

O Vite sobe sempre na porta **5173**, a que o backend libera no CORS (`APP_CORS_ALLOWED_ORIGINS`). Se ela estiver ocupada, o comando falha em vez de trocar de porta, porque em outra porta o navegador bloquearia as chamadas à API.

| Comando | O que faz |
|---|---|
| `npm run dev` | Servidor de desenvolvimento com recarga automática |
| `npm run build` | Checa os tipos (`tsc`) e gera o build em `dist/` |
| `npm run preview` | Serve o build de `dist/` na porta 5173 |
| `npm test` | Roda os testes unitários (Vitest) |
| `npm run seed` | Cria categorias, desenvolvedoras, jogos, usuários e avaliações de exemplo pela API |

## Logins para a demonstração

| Perfil | E-mail | Senha |
|---|---|---|
| ADMIN | `admin@vapor.com` | `admin123` |
| USER (5 contas de exemplo) | `ana.demo@vapor.com`, `bruno.demo@vapor.com`, `carla.demo@vapor.com`, `diego.demo@vapor.com`, `elisa.demo@vapor.com` | `demo1234` |

Também dá para criar uma conta nova em **Cadastrar**: o cadastro sempre gera um USER e já entra com ele.

## Telas

| Rota | Quem acessa | O que mostra |
|---|---|---|
| `/` | Todos | Destaque (jogo mais bem avaliado, com ao menos 3 avaliações), filtros por categoria e desenvolvedora, busca, ordenação e paginação. O estado fica na URL |
| `/jogos/:id` | Todos | Detalhe com nota média e avaliações. Quem está logado avalia, edita e remove a própria avaliação; ADMIN remove qualquer uma |
| `/login`, `/cadastro` | Visitantes | Formulários que mostram os erros de validação do backend |
| `/admin` | Só ADMIN | Abas Jogos, Categorias e Desenvolvedoras, com criar, editar e remover |

As capas dos jogos são **geradas no frontend** a partir do título (o backend não guarda imagens): o mesmo jogo sempre recebe a mesma capa.

## Roteiro sugerido para o pitch

1. **Vitrine:** destaque, filtro por categoria e por desenvolvedora, busca e ordenação. Mostre que a URL muda e que o botão Voltar restaura o filtro.
2. **Detalhe sem login:** a nota média e as avaliações são públicas.
3. **Cadastro:** crie uma conta (já entra logado, com JWT) e avalie um jogo sem avaliações. A média muda na hora. Edite e remova a avaliação.
4. **Permissões:** como USER, abra `/admin` e veja "Acesso restrito". No Swagger, tente `POST /api/jogos` com o token de USER para mostrar o 403 do backend.
5. **Painel ADMIN:** entre como ADMIN, crie categoria, desenvolvedora e jogo (mostre as mensagens de validação do backend nos formulários) e tente remover a categoria em uso para ver o 409.
6. **Swagger:** em http://localhost:8081/swagger-ui/index.html, mostre o mesmo fluxo na API e o 409 de avaliação repetida (a tela de detalhe não oferece o formulário de novo para quem já avaliou, então esse erro só aparece pela API).

## Configuração

| Variável | Padrão | Para que serve |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8081/api` | URL base da API. Copie `.env.example` para `.env` para mudar |
| `API_URL` | `http://localhost:8081/api` | Usada só pelo `npm run seed` |
| `SEED_ADMIN_EMAIL`, `SEED_ADMIN_SENHA` | `admin@vapor.com`, `admin123` | Credenciais do ADMIN usadas pelo seed, se você as trocou no backend |

## Estrutura

```
frontend/
├── scripts/
│   ├── seed.mjs            # popula a API (idempotente: só cria o que não existe)
│   └── seed-data.mjs       # dados de exemplo
└── src/
    ├── api/                # client.ts (fetch + token + erros), types.ts e uma função por recurso
    ├── auth/               # AuthContext (sessão no localStorage) e RequireAdmin
    ├── lib/                # capa gerada, formatação, escolha do destaque e hooks
    ├── components/         # componentes reutilizáveis (cards, estrelas, modal, formulários...)
    └── pages/              # telas; pages/admin tem o painel
```

## Detalhes de comportamento

- **Sessão:** o token JWT e o usuário ficam no `localStorage`. Quando a sessão vence, ou quando a API responde 401 a uma chamada autenticada, o frontend desloga e avisa "Sua sessão expirou".
- **Erros da API:** o cliente converte o corpo `{status, message, fieldErrors}` do backend em um `ApiError`; os formulários exibem `fieldErrors` abaixo de cada campo e `message` em um alerta (inclusive o 409, como "a categoria está associada a jogos").
- **Segurança:** esconder o link Admin e a tela `/admin` de quem não é ADMIN é só conveniência de interface. Quem barra as operações é o backend, que responde 403.
- **Offline:** não há fontes nem scripts externos, então a demonstração funciona sem internet (desde que o backend e o banco estejam no ar).

## Dados de demonstração

Títulos e estúdios do seed são reais, mas **preços e datas são aproximados** e as descrições são resumos próprios; servem só para a apresentação. Para recomeçar do zero, apague o banco (`docker compose down -v` na raiz) e suba o backend e o seed de novo.
