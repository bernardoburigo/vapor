# VAPOR — Plano de Execução do Backend

> **Base:** [02-status-do-sistema.md](02-status-do-sistema.md)
> **Estado:** executado em 02/11/2026. Os desvios em relação ao plano original estão marcados com **Desvio:** em cada fase, e o resultado consolidado está em [02-status-do-sistema.md](02-status-do-sistema.md#8-situação-após-a-execução-do-plano).
> **Decisões já tomadas:** JWT com **jjwt + filtro próprio**; **GET** do catálogo e das avaliações **públicos**, escrita só **ADMIN**.

Ordem por dependência: Categoria → Jogo → lacunas → segurança → Swagger/README → verificação. Cada fase termina com `./mvnw test` verde. Não há commits automáticos: commitar e abrir PR é decisão da equipe.

**Contribuição (RNF07):** as Fases 1 e 2 são as entidades de Gabriel Waltrick e Bruno Baldessar, que ainda não têm commits. O ideal é que eles as implementem em branch própria com PR, como foi feito em `BrunoSouza-desenvolvedora` e `JoaoVBL-avaliacao-controller`.

---

## Fase 1 — CRUD de Categoria (RF04) · Etapa 2

Modelo a copiar: `DesenvolvedoraService`, `DesenvolvedoraController` e `DesenvolvedoraRequestDTO`.

- [x] `repository/CategoriaRepository`: `existsByNomeIgnoreCase`, `existsByNomeIgnoreCaseAndIdNot`, `findAllByNomeContainingIgnoreCase`.
- [x] `JogoRepository`: adicionar `existsByCategoriasId(Long)`.
- [x] `dto/CategoriaRequestDTO` (`@NotBlank` nome ≤ 80, `@Size` descrição ≤ 255) e `CategoriaResponseDTO.fromEntity`.
- [x] `service/CategoriaService`: `@Transactional`, `@Slf4j`, 409 em nome duplicado, 409 ao remover categoria vinculada a jogos.
- [x] `controller/CategoriaController` em `/api/categorias`, com `@Tag`, `@Operation` e `@ApiResponses`.
- [x] Remover o comentário-stub de `entity/Categoria`.
- [x] Testes: `CategoriaServiceTest` (padrão do `AvaliacaoServiceTest`).

## Fase 2 — CRUD de Jogo + RF08 + RF09 · Etapas 2 e 4

- [x] `dto/JogoRequestDTO`: `titulo` `@NotBlank` ≤ 150, `descricao`, `preco` `@NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2)`, `dataLancamento`, `desenvolvedoraId` `@NotNull`, `categoriaIds` `@NotEmpty` (regra "uma ou mais categorias").
- [x] `dto/JogoResponseDTO`: dados do jogo + desenvolvedora (id e nome) + categorias + `notaMedia` + `totalAvaliacoes`.
- [x] `JogoRepository` também estende `JpaSpecificationExecutor<Jogo>`. Filtros `categoriaId`, `desenvolvedoraId` e `titulo` em `Specification`, porque o padrão `(:p IS NULL OR …)` com parâmetro `Long` é frágil no PostgreSQL com Hibernate 7.
- [x] `AvaliacaoRepository`: adicionar `existsByJogoId` e o cálculo da nota média (arredondada para 1 casa; `null` sem avaliações). **Desvio:** em vez de `countByJogoId` e de reutilizar `calcularMediaPorJogoId` por jogo, foi criada `resumirNotasPorJogo`, uma única query agrupada (média e total para vários jogos), que evita N+1 nas listagens paginadas. `calcularMediaPorJogoId` ficou sem uso e foi removida.
- [x] `service/JogoService`: valida desenvolvedora (404) e todas as categorias (404 listando as ausentes); 409 ao remover jogo com avaliações; as linhas de `jogo_categoria` saem junto com o delete.
- [x] `controller/JogoController` em `/api/jogos`, com `?categoriaId=&desenvolvedoraId=&titulo=` e `Pageable` (mesmo padrão de `UsuarioController.listarTodos`).
- [x] Remover o comentário-stub de `entity/Jogo` e de `JogoRepository`.
- [x] Testes: `JogoServiceTest` e um teste de integração do filtro e da nota média.

## Fase 3 — Fechar lacunas dos CRUDs existentes · Etapa 4

- [x] `GlobalExceptionHandler`: handlers para `NoResourceFoundException` (404), `HttpRequestMethodNotSupportedException` (405), `HttpMediaTypeNotSupportedException` (415), `MissingServletRequestParameterException` (400) e `DataIntegrityViolationException` (409).
- [x] `UsuarioService.deletar`: 409 explícito se o usuário tem avaliações (`AvaliacaoRepository.existsByUsuarioId`), com o handler 409 como rede de segurança.
- [x] E-mail normalizado (`trim().toLowerCase()`) em criar, atualizar e login.
- [x] `UsuarioService` e `AvaliacaoService` com `@Transactional` e `@Slf4j`, seguindo `DesenvolvedoraService`. Header `Location` no 201 dos demais controllers.
- [x] Testes novos: `DesenvolvedoraServiceTest` e integração de Desenvolvedora e Avaliacao. **Desvio:** os testes de integração (Desenvolvedora, Avaliacao e dos handlers) foram escritos na Fase 4, porque com a segurança ativa uma rota inexistente anônima responde 401 em vez de 404 e as escritas exigem token.

## Fase 4 — JWT + Spring Security · Etapa 3

1. [x] **Spike (primeiro passo):** no `pom.xml`, adicionar `spring-boot-starter-security` (substitui `spring-security-crypto`), `jjwt-api`, `jjwt-impl` e `jjwt-jackson` 0.12.6 (runtime) e `spring-security-test`. Validar com `dependency:tree` e `contextLoads` que o jjwt convive com o Jackson 3 do Spring Boot 4. Se houver conflito, trocar por `jjwt-gson` ou `jjwt-orgjson`.
2. [x] `application.properties`: `app.jwt.secret=${JWT_SECRET:<padrão de dev com ≥ 32 bytes>}` e `app.jwt.expiration-minutes=${JWT_EXPIRATION_MINUTES:60}`.
3. [x] Pacote `security/`: `JwtService` (gerar e validar; claims `sub` = e-mail, `id`, `role`), `JwtAuthenticationFilter` (`OncePerRequestFilter`), `UsuarioDetailsService`, `RestAuthenticationEntryPoint` (401) e `RestAccessDeniedHandler` (403), ambos respondendo com `ErrorResponse`. `config/SecurityConfig`: stateless, CSRF desligado, `@EnableMethodSecurity`, filtro antes de `UsernamePasswordAuthenticationFilter`. Manter `PasswordEncoderConfig` separado para evitar dependência circular com `UsuarioService`.
4. [x] **Login:** `POST /api/usuarios/login` mantém a validação BCrypt e passa a devolver `AuthResponseDTO(token, tipo, expiraEm, usuario)`. Atualizar o Swagger e o teste `deveLogarComSucesso` (`$.email` vira `$.usuario.email`).
5. [x] **Regras de acesso:**
   - Público: `POST /api/usuarios`, `POST /api/usuarios/login`, `/v3/api-docs/**`, `/swagger-ui/**` e todos os `GET` de `/api/jogos`, `/api/categorias`, `/api/desenvolvedoras`, `/api/avaliacoes`.
   - `POST`, `PUT` e `DELETE` de jogos, categorias e desenvolvedoras: ADMIN.
   - Usuário: listar e deletar só ADMIN; `GET` e `PUT /{id}` do próprio usuário ou ADMIN (`@PreAuthorize`).
   - Avaliacao: criar exige login; o `usuarioId` passa a vir do token (remover o campo de `AvaliacaoRequestDTO` e ajustar `AvaliacaoServiceTest`); alterar e remover só o dono ou ADMIN (checagem no service, 403 via `BusinessException`).
6. [x] **`AccessDeniedException` → 403** no `GlobalExceptionHandler`. Sem isso, o handler `Exception.class` transforma o 403 do `@PreAuthorize` em 500.
7. [x] **ADMIN inicial:** `AdminBootstrap implements ApplicationRunner`, idempotente, lendo `app.admin.email` e `app.admin.senha` (padrão de desenvolvimento `admin@vapor.com` / `admin123`) e usando o `PasswordEncoder`, sem hash commitado. Opcional: `V3__seed_catalogo.sql` com categorias, desenvolvedoras e jogos de exemplo para a demonstração. **Desvio:** o seed não foi feito. Um `INSERT` em `categoria` (nome único) quebraria a inicialização nos bancos dos colegas que já tenham uma categoria com o mesmo nome; o catálogo é preenchido pelo ADMIN via Swagger.
8. [x] **Testes:**
   - Ajustar `UsuarioControllerIntegrationTest` com um helper que faz login e envia `Authorization: Bearer`.
   - Novos: `JwtServiceTest` e `SecurityIntegrationTest` (401 sem token, 401 com token adulterado ou expirado, 403 de USER em endpoint ADMIN, 200 de ADMIN, dono × não dono em Avaliacao).
   - `OpenApiDocsTest` só muda se a configuração de segurança do Swagger mudar.

## Fase 5 — Swagger + ajustes finais · Etapa 5

- [x] Swagger em `AvaliacaoController` (`@Tag`, `@Operation`, `@ApiResponses` 400/401/403/404/409) e em `DesenvolvedoraController` (`@ApiResponses`). Em todos os controllers, `@SecurityRequirement(name = "bearerAuth")` por método nos endpoints protegidos, como em `UsuarioController`. Não usar segurança global, porque o `OpenApiDocsTest` espera `security` por operação.
- [x] `@Schema(description, example)` nos DTOs de Avaliacao, Categoria, Jogo e `AuthResponseDTO`.
- [x] Estender `OpenApiDocsTest` para os 5 recursos e para `bearerAuth` nas escritas.
- [x] Logs (bônus do enunciado): operações importantes em todos os services e login ok/falha, sem logar senha.
- [x] `pom.xml`: preencher `<name>` e `<description>`; remover `<licenses>`, `<developers>` e `<scm>` vazios.
- [x] **Não renomear** `V2__create_avaliacao.sql`: o Flyway guarda o checksum e quebraria os bancos dos colegas. Só documentar no README que ele cria cinco tabelas.
- [x] **README:** estrutura de pastas; tabela de endpoints com permissão; exemplos de request/response (login, criar jogo, criar avaliação com token); credenciais (admin e banco); variáveis `JWT_SECRET`, `JWT_EXPIRATION_MINUTES` e `APP_ADMIN_*`; lista correta de migrations.
- [x] Opcional: acrescentar a seção 9 do PDF ao `01-planejamento-e-modelagem.md`. **Desvio:** não feito; a seção descreve a migration de exemplo (`V1__create_cliente.sql`), que já foi substituída, e ficaria desatualizada.

## Fase 6 — Verificação final e entrega

- [x] `docker compose up -d` e conferir `healthy` em `vapor-postgres`.
- [x] `./mvnw clean test` verde; depois `./mvnw clean package` e `java -jar target/trabalho1-0.0.1-SNAPSHOT.jar`. Resultado: 178 testes, 0 falhas.
- [x] Roteiro manual (**Desvio:** executado por `curl` contra o jar, não clicando no Swagger UI; o UI foi verificado apenas quanto a responder 200):
  1. Cadastrar usuário e fazer login.
  2. Clicar em Authorize com o token.
  3. Como USER, tentar `POST /api/jogos`: esperado 403.
  4. Como ADMIN, criar categoria, desenvolvedora e jogo.
  5. Como USER, criar avaliação e ver `notaMedia` em `GET /api/jogos/{id}`.
  6. Repetir a avaliação: esperado 409.
  7. Filtrar `GET /api/jogos?categoriaId=…`.
  8. Sem token: esperado 401 em endpoint protegido.
- [x] Checklist contra o enunciado: 5 CRUDs, relacionamentos 1:N e N:N, JWT, validações, `@ControllerAdvice`, DTOs, logs, itens do README.
- [x] Pendências fora do backend, registradas na seção 8 do documento de status: protótipo no Figma, frontend, pitch e commits de Gabriel e Bruno Baldessar.

---

## Mapa de arquivos

- **Novos:** `controller/{Categoria,Jogo}Controller`, `service/{Categoria,Jogo}Service`, `repository/CategoriaRepository`, `dto/{Categoria,Jogo,AuthResponse}*DTO`, `security/*`, `config/SecurityConfig`.
- **Alterados:** `pom.xml`, `application.properties`, `UsuarioService`, `UsuarioController`, `AvaliacaoService`, `AvaliacaoController`, `AvaliacaoRequestDTO`, `JogoRepository`, `AvaliacaoRepository`, `GlobalExceptionHandler`, `README.md`.
- **Padrões a reutilizar:** `DesenvolvedoraService` (transação, log, 409), `UsuarioController` (Swagger completo, paginação), `ErrorResponse`, `BusinessException`, `ResourceNotFoundException`.
