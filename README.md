# VAPOR

API REST da plataforma de distribuição digital de jogos VAPOR, inspirada na Steam. Jogadores se cadastram, exploram um catálogo de jogos organizado por categorias e desenvolvedoras e avaliam o que jogaram; cada jogo acumula uma nota média pública.

Stack: Java 21, Spring Boot 4, Spring Data JPA, Spring Security com JWT, PostgreSQL, Flyway, Lombok e Swagger (springdoc).

O planejamento e a modelagem estão em [docs/01-planejamento-e-modelagem.md](docs/01-planejamento-e-modelagem.md). O estado da implementação e o plano de execução estão em [docs/02-status-do-sistema.md](docs/02-status-do-sistema.md) e [docs/03-plano-de-execucao.md](docs/03-plano-de-execucao.md).

## Pré-requisitos

- JDK 21 ou superior
- Docker, para subir o PostgreSQL (ou PostgreSQL 12 a 17 instalado localmente)

Não é preciso instalar o Maven: o projeto usa o Maven Wrapper (`mvnw`).

## Início rápido

```bash
docker compose up -d      # sobe o PostgreSQL
./mvnw spring-boot:run    # sobe a API em http://localhost:8081
```

Depois, abra o Swagger UI em http://localhost:8081/swagger-ui/index.html. Para testar os endpoints protegidos: faça login em `POST /api/usuarios/login`, copie o `token`, clique em **Authorize** e cole o token.

Para ver a loja funcionando, suba também o [frontend](frontend/README.md) (precisa do Node 20.19+):

```bash
cd frontend && npm install && npm run seed && npm run dev   # http://localhost:5173
```

## Credenciais

| Para quê | Usuário | Senha |
|---|---|---|
| Login na API como ADMIN | `admin@vapor.com` | `admin123` |
| PostgreSQL (desenvolvimento) | `postgres` | `postgres` (banco `trabalho1`, porta `5434`) |

O ADMIN é criado automaticamente na primeira inicialização, caso ainda não exista. Usuários cadastrados por `POST /api/usuarios` sempre têm o perfil `USER`. **Essas credenciais são só para desenvolvimento:** em produção, defina `APP_ADMIN_EMAIL`, `APP_ADMIN_SENHA` e `JWT_SECRET` (veja [Variáveis de ambiente](#variáveis-de-ambiente)).

## Banco de dados

O banco roda em um container Docker definido no `docker-compose.yml` da raiz:

```bash
docker compose up -d
```

O PostgreSQL 16 fica disponível em `localhost:5434`.

| Comando | Efeito |
|---|---|
| `docker compose up -d` | Sobe o banco em segundo plano (os dados ficam no volume `vapor-postgres-data`) |
| `docker compose ps` | Mostra se o container está `healthy` |
| `docker compose stop` | Para o container sem apagar os dados |
| `docker compose down` | Remove o container, mantendo os dados |
| `docker compose down -v` | **Remove o container e apaga todos os dados** |

As migrations do Flyway (`src/main/resources/db/migration`) são aplicadas automaticamente quando a aplicação sobe, então não há script separado para criar as tabelas.

Se a porta 5434 já estiver ocupada, troque a porta do banco e aponte a aplicação para ela:

```bash
POSTGRES_PORT=5435 docker compose up -d
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5435/trabalho1 ./mvnw spring-boot:run
```

### Migrations

- `V1__create_usuario.sql`: tabela `usuario`
- `V2__create_avaliacao.sql`: tabelas `desenvolvedora`, `categoria`, `jogo`, `jogo_categoria` e `avaliacao` (o nome do arquivo é histórico; ele cria as cinco tabelas e **não deve ser renomeado**, porque o Flyway guarda o checksum do arquivo)

## Autenticação e autorização

A API é stateless: não há sessão no servidor. O login devolve um token JWT (HS256, válido por 1 hora por padrão) que deve ser enviado em toda requisição protegida:

```
Authorization: Bearer <token>
```

O usuário é recarregado do banco a cada requisição, então um usuário removido perde o acesso na hora, sem esperar o token expirar. Senhas são armazenadas com BCrypt.

| Recurso | Sem login | USER | ADMIN |
|---|---|---|---|
| Cadastro e login (`POST /api/usuarios`, `POST /api/usuarios/login`) | ✅ | ✅ | ✅ |
| Swagger e `/v3/api-docs` | ✅ | ✅ | ✅ |
| Ler jogos, categorias, desenvolvedoras e avaliações (`GET`) | ✅ | ✅ | ✅ |
| Criar, alterar e remover jogos, categorias e desenvolvedoras | ❌ | ❌ | ✅ |
| Avaliar um jogo (`POST /api/avaliacoes`) | ❌ | ✅ | ✅ |
| Alterar ou remover uma avaliação | ❌ | só as próprias | qualquer uma |
| Ver ou alterar um usuário (`GET`/`PUT /api/usuarios/{id}`) | ❌ | só o próprio | qualquer um |
| Listar ou remover usuários (`GET /api/usuarios`, `DELETE`) | ❌ | ❌ | ✅ |

Erros de segurança usam o mesmo formato dos demais erros: **401** quando o token está ausente, inválido ou expirado; **403** quando o usuário está logado, mas o perfil não permite a operação.

## Endpoints

Base: `http://localhost:8081/api`. A documentação completa, com exemplos e todos os códigos de resposta, está no Swagger UI.

| Recurso | Endpoints |
|---|---|
| Usuários | `POST /usuarios`, `POST /usuarios/login`, `GET /usuarios` (paginado), `GET`/`PUT`/`DELETE /usuarios/{id}` |
| Desenvolvedoras | `POST`/`GET /desenvolvedoras` (filtros `?nome=` e `?paisOrigem=`), `GET`/`PUT`/`DELETE /desenvolvedoras/{id}` |
| Categorias | `POST`/`GET /categorias` (filtro `?nome=`), `GET`/`PUT`/`DELETE /categorias/{id}` |
| Jogos | `POST /jogos`, `GET /jogos` (paginado; filtros `?categoriaId=`, `?desenvolvedoraId=` e `?titulo=`, combináveis), `GET`/`PUT`/`DELETE /jogos/{id}` |
| Avaliações | `POST`/`GET /avaliacoes` (filtros `?jogoId=` e `?usuarioId=`), `GET`/`PUT`/`DELETE /avaliacoes/{id}` |

Regras de negócio:

- Um usuário só avalia o mesmo jogo uma vez (**409** se repetir). A nota vai de 1 a 5.
- Todo jogo tem uma desenvolvedora e ao menos uma categoria.
- `GET /jogos` e `GET /jogos/{id}` retornam `notaMedia` (uma casa decimal; `null` se não houver avaliações) e `totalAvaliacoes`.
- Não é possível remover (**409**) uma desenvolvedora ou categoria que tenha jogos, um jogo que tenha avaliações, nem um usuário que tenha avaliações.
- Nomes de desenvolvedoras e de categorias são únicos, sem diferenciar maiúsculas de minúsculas. E-mails são guardados em minúsculas.

## Testando pelo Swagger (passo a passo)

O Swagger UI fica em http://localhost:8081/swagger-ui/index.html e documenta os 26 endpoints, agrupados por recurso (Jogos, Categorias, Desenvolvedoras, Avaliações e Usuários). Um **cadeado** ao lado do endpoint indica que ele exige token; sem cadeado, o endpoint é público.

1. **Endpoint público.** Abra `GET /api/jogos`, clique em **Try it out** e depois em **Execute**. Os valores padrão (`page=0`, `size=20`, `sort=titulo,ASC`) já funcionam. Os filtros (`categoriaId`, `desenvolvedoraId`, `titulo`) começam vazios.
2. **Login.** Abra `POST /api/usuarios/login` e clique em **Try it out**. No seletor **Examples**, acima do corpo, escolha `ADMIN (demonstração)` ou `USER (demonstração)` e clique em **Execute**. Copie o valor de `token` da resposta, sem as aspas.
3. **Authorize.** Clique em **Authorize** (canto superior direito), cole **só o token** (o Swagger acrescenta o `Bearer `), clique em **Authorize** e depois em **Close**. A partir daí, todas as requisições levam o header `Authorization`.
4. **Endpoints protegidos.** Com o token de ADMIN: `GET /api/usuarios` retorna 200. Para criar, use `POST /api/categorias`, `POST /api/desenvolvedoras` e `POST /api/jogos` (os exemplos de corpo trazem ids ilustrativos: troque pelos ids reais que aparecem em `GET /api/categorias` e `GET /api/desenvolvedoras`).
5. **Mostrar o 403.** Faça login com `USER (demonstração)`, clique em **Authorize**, depois em **Logout**, cole o novo token e confirme. Agora `POST /api/categorias` e `GET /api/usuarios` retornam **403**.
6. **Mostrar o 401.** Em **Authorize**, clique em **Logout** e execute `GET /api/usuarios`: retorna **401**.

Observações:

- Os campos `id` dos endpoints de `GET`, `PUT` e `DELETE` por id começam **vazios** de propósito, para um `DELETE` não sair executando sobre um id qualquer. Pegue o id na listagem correspondente.
- O token vale 60 minutos; depois disso as chamadas protegidas voltam a dar 401 e é preciso fazer login de novo.
- O `USER (demonstração)` (`ana.demo@vapor.com`) só existe depois de rodar `npm run seed` no [frontend](frontend/README.md). Sem o seed, cadastre um usuário em `POST /api/usuarios` e use-o no login.
- Os exemplos de login contêm as credenciais de desenvolvimento do README. **Remova-os em produção** (`UsuarioController`, constantes `EXEMPLO_LOGIN_ADMIN` e `EXEMPLO_LOGIN_USER`).
- Se mudou o código do backend, reinicie a aplicação para o Swagger refletir a mudança.

## Exemplos de requisições e respostas

### Login

```bash
curl -X POST http://localhost:8081/api/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@vapor.com","senha":"admin123"}'
```

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEmSegundos": 3600,
  "usuario": {
    "id": 1,
    "nome": "Administrador",
    "email": "admin@vapor.com",
    "role": "ADMIN",
    "dataCadastro": "2026-11-02T21:32:32.021737"
  }
}
```

### Cadastrar usuário (público)

```bash
curl -X POST http://localhost:8081/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"Bernardo Búrigo","email":"bernardo@vapor.com","senha":"senha123"}'
```

Resposta `201 Created`, com o header `Location: /api/usuarios/{id}`:

```json
{
  "id": 2,
  "nome": "Bernardo Búrigo",
  "email": "bernardo@vapor.com",
  "role": "USER",
  "dataCadastro": "2026-11-02T21:35:45.303694"
}
```

### Criar jogo (ADMIN)

```bash
curl -X POST http://localhost:8081/api/jogos \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"titulo":"Half-Life 3","descricao":"O retorno de Gordon Freeman","preco":199.90,
       "dataLancamento":"2026-11-15","desenvolvedoraId":1,"categoriaIds":[1]}'
```

Resposta `201 Created`:

```json
{
  "id": 1,
  "titulo": "Half-Life 3",
  "descricao": "O retorno de Gordon Freeman",
  "preco": 199.9,
  "dataLancamento": "2026-11-15",
  "desenvolvedora": { "id": 1, "nome": "Valve Corporation" },
  "categorias": [{ "id": 1, "nome": "RPG", "descricao": "Jogos de interpretação de papéis" }],
  "notaMedia": null,
  "totalAvaliacoes": 0
}
```

### Avaliar um jogo (usuário logado)

O autor é o usuário do token, não vai no corpo.

```bash
curl -X POST http://localhost:8081/api/avaliacoes \
  -H "Authorization: Bearer $TOKEN_USER" -H "Content-Type: application/json" \
  -d '{"jogoId":1,"nota":5,"comentario":"Obra-prima!"}'
```

Resposta `201 Created`:

```json
{
  "id": 1,
  "usuarioId": 2,
  "usuarioNome": "Bernardo Búrigo",
  "jogoId": 1,
  "jogoTitulo": "Half-Life 3",
  "nota": 5,
  "comentario": "Obra-prima!",
  "dataAvaliacao": "2026-11-02T21:35:45.502394"
}
```

### Listar jogos de uma categoria (público)

```bash
curl "http://localhost:8081/api/jogos?categoriaId=1&page=0&size=20"
```

```json
{
  "content": [ { "id": 1, "titulo": "Half-Life 3", "notaMedia": 5.0, "totalAvaliacoes": 1 } ],
  "page": { "size": 20, "number": 0, "totalElements": 1, "totalPages": 1 }
}
```

(Em cada item de `content`, os demais campos são os mesmos do exemplo de criação de jogo.)

### Formato de erro

Todos os erros seguem o mesmo formato. Validação (`400`) inclui `fieldErrors`:

```json
{
  "timestamp": "2026-11-02T21:35:45.573409435",
  "status": 409,
  "error": "Conflict",
  "message": "Usuário já avaliou este jogo",
  "fieldErrors": null
}
```

```json
{
  "timestamp": "2026-11-02T21:35:45.430412442",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados inválidos",
  "fieldErrors": { "email": "Email inválido", "senha": "Senha deve ter entre 6 e 100 caracteres" }
}
```

## Comandos

Todos os comandos são executados na raiz do projeto. No Windows, substitua `./mvnw` por `mvnw.cmd`.

| Objetivo | Comando | Observação |
|---|---|---|
| Rodar o backend com Swagger | `./mvnw spring-boot:run` | Sobe a API em modo de desenvolvimento |
| Rodar os testes | `./mvnw test` | Inclui testes de integração, que usam o banco real: o PostgreSQL precisa estar no ar |
| Gerar o jar executável | `./mvnw clean package` | Roda os testes e gera `target/trabalho1-0.0.1-SNAPSHOT.jar` |
| Rodar o jar gerado | `java -jar target/trabalho1-0.0.1-SNAPSHOT.jar` | Precisa de `package` antes |
| Limpar a pasta de build | `./mvnw clean` | Remove `target/` |

Com `java -jar`, você pode sobrescrever propriedades pela linha de comando:

```bash
java -jar target/trabalho1-0.0.1-SNAPSHOT.jar --server.port=8082 --spring.datasource.url=jdbc:postgresql://localhost:5434/trabalho1
```

Endereços com a aplicação no ar (porta padrão `8081`):

- Swagger UI: http://localhost:8081/swagger-ui/index.html
- Especificação OpenAPI (JSON): http://localhost:8081/v3/api-docs

## Testes

```bash
./mvnw test
```

A suíte cobre as regras de negócio dos services (com mocks), os controllers de ponta a ponta contra o PostgreSQL real (com rollback ao final de cada teste), a segurança (401, 403, token expirado, adulterado, usuário removido, dono ou ADMIN) e a documentação OpenAPI.

## Estrutura de pastas

```
.
├── docker-compose.yml            # PostgreSQL 16 para desenvolvimento
├── pom.xml
├── docs/                         # análise, status e plano de execução (+ PDFs do trabalho)
├── frontend/                     # loja em React + Vite + Tailwind (veja frontend/README.md)
└── src
    ├── main
    │   ├── java/com/example/trabalho1
    │   │   ├── config/           # SecurityConfig, OpenApiConfig, AdminBootstrap, encoder BCrypt
    │   │   ├── controller/       # endpoints REST e documentação Swagger
    │   │   ├── dto/              # objetos de entrada e saída (nenhuma entity é exposta)
    │   │   ├── entity/           # entidades JPA e enum Role
    │   │   ├── exception/        # GlobalExceptionHandler (@RestControllerAdvice) e ErrorResponse
    │   │   ├── repository/       # Spring Data JPA
    │   │   ├── security/         # JwtService, filtro JWT, UserDetailsService, handlers 401/403
    │   │   └── service/          # regras de negócio
    │   └── resources
    │       ├── application.properties
    │       └── db/migration/     # migrations do Flyway
    └── test/java/com/example/trabalho1   # testes unitários e de integração
```

Fluxo de uma requisição: filtro JWT → `Controller` (valida o DTO com `@Valid`) → `Service` (regras de negócio) → `Repository` → PostgreSQL. Erros lançados em qualquer camada são convertidos em JSON pelo `GlobalExceptionHandler`.

## Variáveis de ambiente

Os valores padrão estão em `src/main/resources/application.properties`. Para usar outra porta, outro banco ou outros segredos sem alterar o arquivo, defina variáveis de ambiente antes do comando:

| Variável | Padrão | Para que serve |
|---|---|---|
| `SERVER_PORT` | `8081` | Porta da API |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5434/trabalho1` | URL do banco |
| `POSTGRES_PORT` | `5434` | Porta exposta pelo `docker compose` |
| `JWT_SECRET` | segredo de desenvolvimento | Chave de assinatura dos tokens. **Defina em produção**; precisa ter ao menos 32 bytes (32 caracteres ASCII) |
| `JWT_EXPIRATION_MINUTES` | `60` | Validade do token |
| `APP_ADMIN_EMAIL` | `admin@vapor.com` | E-mail do ADMIN criado na inicialização |
| `APP_ADMIN_SENHA` | `admin123` | Senha do ADMIN criado na inicialização |
| `APP_ADMIN_NOME` | `Administrador` | Nome do ADMIN criado na inicialização |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:4200,http://localhost:5173` | Origens do frontend liberadas no CORS |

```bash
SERVER_PORT=8082 JWT_SECRET=troque-por-um-segredo-longo-e-aleatorio-com-32-ou-mais ./mvnw spring-boot:run
```

Com `spring-boot:run`, use variáveis de ambiente: propriedades passadas com `-D` ao Maven não chegam ao processo da aplicação.

## Frontend

A loja em React que consome esta API está em [`frontend/`](frontend/README.md): catálogo com destaque, filtros e busca, detalhe do jogo com nota média e avaliações, login e cadastro, e um painel de administração (jogos, categorias e desenvolvedoras).

| Item | Situação |
|---|---|
| Tecnologia | React 19 + TypeScript + Vite 7 + Tailwind CSS 4 |
| Diretório no repositório | `frontend/` |
| Instalação das dependências | `cd frontend && npm install` (Node 20.19 ou superior) |
| Dados de demonstração | `npm run seed` (com o backend no ar; pode ser repetido sem duplicar) |
| Execução em desenvolvimento | `npm run dev` |
| URL local | http://localhost:5173 (porta liberada no CORS do backend) |
| Integração com a API | `http://localhost:8081/api`, configurável por `VITE_API_URL` |
| Logins de demonstração | ADMIN `admin@vapor.com` / `admin123`; USER `ana.demo@vapor.com` / `demo1234` |
