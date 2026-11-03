# VAPOR — Status da Implementação do Backend

> **Data da análise:** 02/11/2026 (diagnóstico **inicial**, anterior à execução do plano; a situação final está na [seção 8](#8-situação-após-a-execução-do-plano))
> **Base de comparação:** código de `main` (HEAD `ec8afaf`) × [docs/01-planejamento-e-modelagem.md](01-planejamento-e-modelagem.md) × enunciado (`docs/TRABALHO 1 - TURMA 1.pdf`)
> **Legenda:** ✅ pronto · 🟡 incompleto · ❌ faltando · ⚠️ risco

**Limites desta análise:** o código foi lido, não executado. Existem 42 testes escritos (11 em `UsuarioServiceTest`, 13 em `AvaliacaoServiceTest`, 12 em `UsuarioControllerIntegrationTest`, 5 em `OpenApiDocsTest` e 1 `contextLoads`), mas nenhum foi rodado para esta análise. Os testes de integração exigem o PostgreSQL no ar (`docker compose up -d`).

As 8 branches remotas não têm commits fora de `main`, então não há trabalho pendente de integração.

---

## 1. Resumo por etapa do cronograma

| Etapa | Situação | Resumo |
|---|---|---|
| 1. Documento + DER | ✅ Pronta | `docs/01-*.md` e o PDF estão completos. O `.md` não tem a seção 9 "Nota de Escopo" que existe no PDF |
| 2. CRUDs básicos | 🟡 3 de 5 | Usuario, Desenvolvedora e Avaliacao funcionam. **Categoria e Jogo não têm CRUD** (o enunciado exige 5 entidades) |
| 3. JWT + Spring Security | ❌ 0% | Nenhum código de segurança. O `pom.xml` só tem `spring-security-crypto` (BCrypt) |
| 4. Aplicação das Camadas | 🟡 Parcial | Camadas completas para 3 entidades. Categoria e Jogo só têm entity (e um repository mínimo) |
| 5. Swagger + Ajustes | 🟡 Parcial | springdoc configurado. Só Usuario está bem documentado; README incompleto |

---

## 2. ✅ Pronto

- **Banco e Flyway (RNF03):** [V1__create_usuario.sql](../src/main/resources/db/migration/V1__create_usuario.sql) cria `usuario`. [V2__create_avaliacao.sql](../src/main/resources/db/migration/V2__create_avaliacao.sql) cria as outras cinco tabelas (`desenvolvedora`, `categoria`, `jogo`, `jogo_categoria`, `avaliacao`), com FKs, `CHECK (nota BETWEEN 1 AND 5)` e `UNIQUE (usuario_id, jogo_id)`. O schema bate com o dicionário de dados. `ddl-auto=validate` em [application.properties](../src/main/resources/application.properties).
- **Entities:** as cinco mais o enum [Role](../src/main/java/com/example/trabalho1/entity/Role.java) (`USER`, `ADMIN`). Relacionamentos mapeados: `Jogo` → `Desenvolvedora` (`@ManyToOne`, 1:N) e `Jogo` ↔ `Categoria` (`@ManyToMany` via `jogo_categoria`, N:N). `Avaliacao` liga `Usuario` e `Jogo`.
- **CRUD de Usuario (RF02):** [UsuarioController](../src/main/java/com/example/trabalho1/controller/UsuarioController.java), [UsuarioService](../src/main/java/com/example/trabalho1/service/UsuarioService.java), repository, três DTOs, paginação (`Pageable`, resposta via `PageSerializationMode.VIA_DTO`), validações, Swagger completo (exemplos e respostas 400/401/404/409), 11 testes de service e 12 de integração.
- **CRUD de Desenvolvedora (RF03):** [DesenvolvedoraController](../src/main/java/com/example/trabalho1/controller/DesenvolvedoraController.java) e [DesenvolvedoraService](../src/main/java/com/example/trabalho1/service/DesenvolvedoraService.java) com `@Transactional` e `@Slf4j`, filtros por nome e por país, 409 em nome duplicado e 409 ao remover desenvolvedora que ainda tem jogos.
- **CRUD de Avaliacao (RF06) e unicidade (RF07):** [AvaliacaoController](../src/main/java/com/example/trabalho1/controller/AvaliacaoController.java) e [AvaliacaoService](../src/main/java/com/example/trabalho1/service/AvaliacaoService.java). Nota validada de 1 a 5, comentário até 2000 caracteres, 409 quando o usuário já avaliou o jogo (checagem no service, mais a constraint do banco), filtros `?jogoId=` e `?usuarioId=`, 13 testes de service.
- **Tratamento de erros (RF10, parcial):** [GlobalExceptionHandler](../src/main/java/com/example/trabalho1/exception/GlobalExceptionHandler.java) (`@RestControllerAdvice`) cobre validação (com `fieldErrors`), `ResourceNotFoundException` (404), `BusinessException` (status configurável), JSON malformado, tipo de parâmetro inválido e erro genérico (500), tudo no formato padrão [ErrorResponse](../src/main/java/com/example/trabalho1/exception/ErrorResponse.java).
- **DTOs (RNF02):** nenhum controller expõe entity.
- **BCrypt (RNF05):** [PasswordEncoderConfig](../src/main/java/com/example/trabalho1/config/PasswordEncoderConfig.java) e `UsuarioService`.
- **Nomenclatura (RNF08):** `snake_case` no banco, `camelCase` no Java, nomes em português.
- **Infraestrutura:** [docker-compose.yml](../docker-compose.yml) (PostgreSQL 16 na porta 5434), [README](../README.md) com execução e variáveis, [OpenApiConfig](../src/main/java/com/example/trabalho1/config/OpenApiConfig.java) com o esquema `bearerAuth` declarado, [OpenApiDocsTest](../src/test/java/com/example/trabalho1/config/OpenApiDocsTest.java).

---

## 3. 🟡 Incompleto

| Item | O que existe | O que falta |
|---|---|---|
| **RF01 — Login com JWT** | Cadastro e login validando a senha com BCrypt | `POST /api/usuarios/login` devolve `UsuarioResponseDTO`, não um token |
| **RF09 — Nota média** | `AvaliacaoRepository.calcularMediaPorJogoId` (query pronta) | Nada chama a query: não há `JogoService` nem campo de nota média em DTO |
| **Categoria** | Só a entity, com o comentário "Declarada aqui para satisfazer…" | Repository, DTOs, service, controller, testes |
| **Jogo** | Entity e `JogoRepository` com um único método (`existsByDesenvolvedoraId`) | DTOs, service, controller, filtros, testes, nota média |
| **Avaliacao** | CRUD funcional | Nenhuma anotação Swagger. O `usuarioId` vem do corpo da requisição (deve vir do token). Sem checagem de dono. Sem `@Transactional` nem log. Sem teste de integração |
| **Desenvolvedora** | CRUD funcional | **Nenhum teste.** Swagger só com `@Operation` (sem `@ApiResponses` nem segurança) |
| **Usuario** | CRUD completo | Excluir usuário que tem avaliações viola a FK e cai no handler genérico, devolvendo **500**. E-mail é case-sensitive (`A@x.com` e `a@x.com` coexistem). Sem `@Transactional` nem log |
| **Exception handler** | Cobre os casos principais | URL inexistente (`NoResourceFoundException`), 405, 415 e `DataIntegrityViolationException` caem em `Exception.class` e viram 500. Falta `AccessDeniedException`, obrigatório quando houver segurança |
| **RNF06 — Swagger** | springdoc, esquema `bearerAuth`, Usuario e Desenvolvedora documentados | Avaliacao, Categoria e Jogo sem documentação. `OpenApiDocsTest` só cobre Usuario |
| **README** | Pré-requisitos, banco, scripts, variáveis, placeholder de Frontend | Faltam estrutura de pastas, exemplos de request/response e credenciais de login (só há as do banco). A lista de migrations está desatualizada: diz que V2 é só `avaliacao`, mas ela cria cinco tabelas |
| **Consistência** | Bons padrões em `DesenvolvedoraService` | Header `Location` no 201 só em Usuario. `@Transactional` e logs só em Desenvolvedora. Comentários-stub nas entities e em `JogoRepository`. `pom.xml` com `<name/>`, `<license/>`, `<developer/>` e `<scm/>` vazios |

---

## 4. ❌ Faltando

- **RF04 — CRUD de Categoria:** repository, DTOs, service, controller, testes.
- **RF05 — CRUD de Jogo:** DTOs, service, controller, regras de vínculo com desenvolvedora e categorias, testes.
- **RF08 — Listar jogos por categoria e/ou desenvolvedora.**
- **RNF04 e Etapa 3 inteira:** `spring-boot-starter-security`, biblioteca JWT, `SecurityFilterChain`, filtro JWT, `UserDetailsService`, respostas 401/403 em JSON, segredo e expiração em `application.properties`, uso efetivo de `Role`, `spring-security-test`.
- **Usuário ADMIN:** `UsuarioService.criar` sempre gera `USER`; hoje não existe forma de obter um ADMIN.
- **Autorização:** nenhuma regra por perfil nem por dono do recurso.
- **README:** itens obrigatórios do enunciado ainda ausentes (estrutura de pastas, exemplos de request/response, credenciais).
- **Fora do escopo do backend, mas exigidos na entrega:** protótipo de telas (Figma), frontend e pitch de 10 a 15 minutos.
- **⚠️ RNF07 — contribuição da equipe:** o `git shortlog` mostra commits apenas de Bernardo, João Victor e Bruno Souza. **Gabriel Waltrick (Categoria) e Bruno Baldessar (Jogo) não têm nenhum commit**, e as entidades deles são justamente as que faltam. O enunciado exige contribuição de todos os membros no GitHub.

---

## 5. Camadas por entidade (Etapa 4)

| Entidade | Responsável | Entity | Repository | DTO | Service | Controller | Validação | Testes | Swagger |
|---|---|---|---|---|---|---|---|---|---|
| Usuario | Bernardo | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ unit + integração | ✅ completo |
| Desenvolvedora | Bruno Souza | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | 🟡 básico |
| Avaliacao | João Vitor | ✅ | ✅ | 🟡 | ✅ | ✅ | ✅ | 🟡 só unit | ❌ |
| Categoria | Gabriel | 🟡 stub | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Jogo | Bruno Baldessar | 🟡 stub | 🟡 1 método | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## 6. Requisitos do documento de análise

| Req. | Status | Observação |
|---|---|---|
| RF01 | 🟡 | Login existe, sem JWT |
| RF02 | ✅ | |
| RF03 | ✅ | Sem testes |
| RF04 | ❌ | |
| RF05 | ❌ | |
| RF06 | 🟡 | Funcional, sem Swagger nem autorização |
| RF07 | ✅ | Service + constraint + teste |
| RF08 | ❌ | |
| RF09 | 🟡 | Só a query existe |
| RF10 | 🟡 | Validação ok; faltam handlers (404 de rota, 405, 415, integridade, 403) |
| RNF01 | 🟡 | Faltam as camadas de Categoria e Jogo |
| RNF02 | ✅ | |
| RNF03 | ✅ | |
| RNF04 | ❌ | |
| RNF05 | ✅ | |
| RNF06 | 🟡 | |
| RNF07 | ⚠️ | Dois integrantes sem commits |
| RNF08 | ✅ | |

---

## 7. Próximo passo

O plano de execução por fases (Categoria → Jogo → lacunas → JWT/Security → Swagger/README → verificação) está em [03-plano-de-execucao.md](03-plano-de-execucao.md).

---

## 8. Situação após a execução do plano

> Atualizado em 02/11/2026, depois de executar as Fases 1 a 6 de [03-plano-de-execucao.md](03-plano-de-execucao.md). As seções 1 a 6 acima descrevem o estado **antes** da execução e foram mantidas como registro.

**Verificação:** `./mvnw clean package` com **178 testes passando, 0 falhas** (PostgreSQL 16 real, Java 25). O jar gerado foi executado e o fluxo completo (cadastro, login, CRUDs, 401/403/409, filtros, nota média) foi exercitado por `curl` contra ele; o Swagger UI respondeu 200.

| Etapa | Antes | Agora |
|---|---|---|
| 1. Documento + DER | ✅ | ✅ |
| 2. CRUDs básicos | 🟡 3 de 5 | ✅ 5 de 5 (Categoria e Jogo implementados) |
| 3. JWT + Spring Security | ❌ | ✅ |
| 4. Aplicação das Camadas | 🟡 | ✅ todas as entidades com Entity, Repository, DTO, Service, Controller, validação e testes |
| 5. Swagger + Ajustes | 🟡 | ✅ os 5 recursos documentados; README completo |

| Req. | Agora | Como foi atendido |
|---|---|---|
| RF01 | ✅ | `POST /api/usuarios/login` devolve token JWT (`AuthResponseDTO`) |
| RF02 a RF06 | ✅ | CRUD completo das 5 entidades |
| RF07 | ✅ | Mantido (service + constraint + teste de integração) |
| RF08 | ✅ | `GET /api/jogos?categoriaId=&desenvolvedoraId=&titulo=` (Specification, paginado) |
| RF09 | ✅ | `notaMedia` e `totalAvaliacoes` em `JogoResponseDTO`, por uma query agrupada |
| RF10 | ✅ | Handlers para 404 de rota, 405, 406, 415, parâmetro ausente, integridade (409), 401 e 403 |
| RNF01, RNF02 | ✅ | Camadas completas; nenhuma entity exposta |
| RNF04 | ✅ | `SecurityFilterChain` stateless, `JwtAuthenticationFilter`, `@EnableMethodSecurity` |
| RNF05 | ✅ | BCrypt; o ADMIN inicial é criado por `AdminBootstrap` sem hash no repositório |
| RNF06 | ✅ | Swagger completo; `OpenApiDocsTest` cobre os 5 recursos e o `bearerAuth` |
| RNF07 | ⚠️ | **Sem mudança.** Gabriel Waltrick e Bruno Baldessar continuam sem commits; Categoria e Jogo foram implementados nesta execução, fora da autoria deles |

**Itens do diagnóstico inicial resolvidos:** excluir usuário com avaliações agora retorna 409 (antes, 500); e-mail normalizado em minúsculas; `@Transactional` e logs em todos os services; `Location` no 201 de todos os controllers; comentários-stub removidos; `pom.xml` sem elementos vazios; README com estrutura de pastas, exemplos, credenciais e migrations corretas; `open-in-view` desligado.

**Ainda pendente (fora do backend ou fora do escopo desta execução):**

- Protótipo das telas (Figma): não feito. O frontend em [`frontend/`](../frontend/README.md) já implementa as telas e pode servir de referência.
- Pitch de 10 a 15 minutos.
- RNF07: commits de Gabriel Waltrick e Bruno Baldessar.
- Os documentos `01-planejamento-e-modelagem.md` e o PDF não foram alterados; o `.md` continua sem a seção 9 "Nota de Escopo" do PDF (agora desatualizada, pois descreve a migration de exemplo que já foi substituída).
- O banco começa vazio. Os dados de demonstração vêm do script `npm run seed` do frontend (via API), não de uma migration do Flyway.

**Frontend (adicionado depois):** loja em React + Vite + Tailwind em `frontend/`, com vitrine, detalhe e avaliações, login e cadastro e painel ADMIN. Não exigiu mudanças no backend. Detalhes e roteiro de demonstração em [frontend/README.md](../frontend/README.md).
