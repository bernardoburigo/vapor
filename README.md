# VAPOR

API REST da plataforma de distribuição digital de jogos VAPOR, inspirada na Steam. O planejamento e a modelagem estão em [docs/01-planejamento-e-modelagem.md](docs/01-planejamento-e-modelagem.md).

## Pré-requisitos

- JDK 21 ou superior
- Docker, para subir o PostgreSQL (ou PostgreSQL 12 a 17 instalado localmente)

Não é preciso instalar o Maven: o projeto usa o Maven Wrapper (`mvnw`).

## Banco de dados

O banco roda em um container Docker definido no `docker-compose.yml` da raiz:

```bash
docker compose up -d
```

O PostgreSQL 16 fica disponível em `localhost:5434`. Credenciais de desenvolvimento: usuário `postgres`, senha `postgres`, banco `trabalho1`.

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

## Scripts

Todos os comandos são executados na raiz do projeto. No Windows, substitua `./mvnw` por `mvnw.cmd`.

| Objetivo | Comando | Observação |
|---|---|---|
| Rodar o backend com Swagger | `./mvnw spring-boot:run` | Sobe a API em modo de desenvolvimento |
| Rodar os testes | `./mvnw test` | Inclui testes de integração, que usam o banco real: o PostgreSQL precisa estar no ar |
| Gerar o jar executável | `./mvnw clean package` | Gera `target/trabalho1-0.0.1-SNAPSHOT.jar` |
| Rodar o jar gerado | `java -jar target/trabalho1-0.0.1-SNAPSHOT.jar` | Precisa de `package` antes |
| Limpar a pasta de build | `./mvnw clean` | Remove `target/` |

### Backend + Swagger

```bash
./mvnw spring-boot:run
```

Com a aplicação no ar (porta padrão `8081`):

- Swagger UI: http://localhost:8081/swagger-ui/index.html
- Especificação OpenAPI (JSON): http://localhost:8081/v3/api-docs
- Base da API de usuários: http://localhost:8081/api/usuarios

Os endpoints de `Usuario` estão documentados no Swagger, com exemplos de requisição e resposta. Os marcados com cadeado (`bearerAuth`) exigirão token JWT quando a autenticação for implementada; cadastro e login são públicos.

### Testes

```bash
./mvnw test
```

### Jar executável

```bash
./mvnw clean package
java -jar target/trabalho1-0.0.1-SNAPSHOT.jar
```

Com `java -jar`, você pode sobrescrever propriedades pela linha de comando:

```bash
java -jar target/trabalho1-0.0.1-SNAPSHOT.jar --server.port=8081 --spring.datasource.url=jdbc:postgresql://localhost:5434/trabalho1
```

### Migrations

Não há script separado. Os arquivos em `src/main/resources/db/migration` são executados pelo Flyway na inicialização da aplicação, em ordem de versão:

- `V1__create_usuario.sql`: tabela `usuario`
- `V2__create_avaliacao.sql`: tabela `avaliacao`

## Variáveis de ambiente

Os valores padrão estão em `src/main/resources/application.properties`. Para usar outra porta ou outro banco sem alterar o arquivo, defina variáveis de ambiente antes do comando:

| Variável | Padrão | Exemplo |
|---|---|---|
| `SERVER_PORT` | `8081` | `SERVER_PORT=8082` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5434/trabalho1` | `jdbc:postgresql://localhost:5435/trabalho1` |
| `POSTGRES_PORT` | `5434` (usada pelo `docker compose`) | `POSTGRES_PORT=5435` |

```bash
SERVER_PORT=8081 SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/trabalho1 ./mvnw spring-boot:run
```

Com `spring-boot:run`, use variáveis de ambiente: propriedades passadas com `-D` ao Maven não chegam ao processo da aplicação.

## Frontend

Espaço reservado para o frontend, que ainda não foi iniciado.

| Item | Situação |
|---|---|
| Tecnologia | A definir |
| Diretório no repositório | A definir |
| Instalação das dependências | A definir |
| Execução em desenvolvimento | A definir |
| URL local | A definir |
| Integração com a API | A definir (base em `http://localhost:8081/api`) |
