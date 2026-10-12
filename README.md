# Reactive Ticketing API

API reativa para cadastro de eventos e venda de ingressos, construída com **Kotlin**, **Spring WebFlux** e **Coroutines**, usando **R2DBC** para acesso não bloqueante ao PostgreSQL.

Além do CRUD tradicional, a API expõe endpoints em **Server-Sent Events (SSE)**: o cliente se conecta uma vez e recebe atualizações em tempo real quando eventos são criados ou quando o estoque de ingressos muda (criação, reposição ou venda).

## Stack

| Tecnologia                             | Uso                              |
|----------------------------------------|----------------------------------|
| Kotlin 2.3 + Java 21                   | Linguagem e runtime              |
| Spring Boot 4.1 (WebFlux)              | Servidor HTTP reativo            |
| Kotlin Coroutines (`suspend` / `Flow`) | Modelo de programação assíncrona |
| Spring Data R2DBC                      | Acesso reativo ao banco          |
| Flyway                                 | Migrations do banco (via JDBC)   |
| PostgreSQL 16                          | Banco de dados                   |
| Docker Compose                         | Subir o banco localmente         |
| Gradle (wrapper)                       | Build                            |

## Pré-requisitos

- **JDK 21**
- **Docker** e **Docker Compose**

Não é necessário instalar o Gradle: o projeto usa o wrapper (`./gradlew`).

## Como executar

**1. Configure as variáveis de ambiente do banco**

```bash
cp env.example .env
```

O arquivo `.env` é lido pelo Docker Compose para criar o banco:

```env
DB_HOST=localhost
DB_NAME=ticketing
DB_USER=postgres
DB_PASSWORD=postgres
```

**2. Suba o PostgreSQL**

```bash
docker compose up -d
```

**3. Rode a aplicação**

```bash
./gradlew bootRun
```

Na inicialização, o Flyway cria as tabelas e insere dados de exemplo (eventos e ingressos). A API fica disponível em `http://localhost:8080`.

**4. Teste**

```bash
curl http://localhost:8080/v1/events
```

> **Sobre as variáveis de ambiente:** o Spring **não** lê o arquivo `.env`. A aplicação usa os valores padrão definidos em `src/main/resources/application.yaml` (que são os mesmos do `env.example`). Se você alterar o `.env`, exporte as mesmas variáveis antes de rodar a aplicação:
>
> ```bash
> set -a && source .env && set +a
> ./gradlew bootRun
> ```

### Rodando os testes

```bash
./gradlew test
```

Os testes sobem o contexto completo do Spring, então o PostgreSQL precisa estar rodando.

### Parando o ambiente

```bash
docker compose down      # para o banco, mantendo os dados
docker compose down -v   # para o banco e apaga os dados (as migrations rodam de novo no próximo start)
```

## Endpoints

| Método   | Rota                              | Descrição                                                  |
|----------|-----------------------------------|------------------------------------------------------------|
| `GET`    | `/v1/events`                      | Lista todos os eventos                                     |
| `GET`    | `/v1/events/{id}`                 | Busca um evento por ID                                     |
| `POST`   | `/v1/events`                      | Cria um evento                                             |
| `DELETE` | `/v1/events/{id}`                 | Remove um evento (e seus ingressos/vendas)                 |
| `GET`    | `/v1/events/categories/{type}`    | **SSE** — eventos de uma categoria + novos eventos criados |
| `GET`    | `/v1/events/{eventId}/tickets`    | **SSE** — ingressos do evento + atualizações de estoque    |
| `POST`   | `/v1/tickets`                     | Cria um tipo de ingresso para um evento                    |
| `PATCH`  | `/v1/tickets/{id}/add?quantity=N` | Repõe ingressos no estoque                                 |
| `POST`   | `/v1/tickets/sale`                | Vende ingressos e registra a venda                         |

Detalhes de cada endpoint (payloads, validações, respostas e erros) estão em [docs/api.md](docs/api.md).

### Exemplos rápidos

Acompanhar o estoque de ingressos de um evento em tempo real (deixe rodando em um terminal):

```bash
curl -N http://localhost:8080/v1/events/3fdfdfd5-f757-495a-a346-a62c1347b919/tickets
```

Em outro terminal, pegue o `id` de um ingresso retornado acima e faça uma venda — o terminal do SSE recebe o estoque atualizado:

```bash
curl -X POST http://localhost:8080/v1/tickets/sale \
  -H "Content-Type: application/json" \
  -d '{"ticketId": "<id-do-ingresso>", "quantity": 2}'
```

Criar um evento:

```bash
curl -X POST http://localhost:8080/v1/events \
  -H "Content-Type: application/json" \
  -d '{"type": "SHOW", "name": "Coldplay", "date": "2026-12-10", "description": "Turnê Music of the Spheres"}'
```

## Estrutura do projeto

O código segue **arquitetura hexagonal (ports & adapters)**, organizada por módulo de negócio:

```
src/main/kotlin/dev/rogerbertan/reactive_ticketing_api/
├── event/                     # Módulo de eventos
│   ├── adapter/
│   │   ├── in/http/           # Controller, DTOs e mappers HTTP
│   │   └── out/persistence/   # Entidades, repositórios R2DBC e adapters
│   └── application/
│       ├── domain/            # Modelo de domínio e exceções
│       ├── port/in/           # Casos de uso (interfaces) e commands
│       ├── port/out/          # Portas de saída (repositórios)
│       └── service/           # Implementação dos casos de uso
├── ticket/                    # Módulo de ingressos e vendas (mesma estrutura)
└── shared/adapter/in/http/    # Tratamento global de erros

src/main/resources/
├── application.yaml           # Configuração (banco, Flyway)
└── db/migration/              # Migrations Flyway (schema + dados de exemplo)
```

## Documentação

| Documento                           | Conteúdo                                                                                                 |
|-------------------------------------|----------------------------------------------------------------------------------------------------------|
| [Arquitetura](docs/architecture.md) | Arquitetura hexagonal, diagramas C4 (contexto, contêineres e componentes) e modelo reativo               |
| [Fluxos](docs/flows.md)             | Diagramas de sequência da venda, reposição, criação de ingressos e streams SSE; controle de concorrência |
| [API](docs/api.md)                  | Referência completa dos endpoints, payloads, validações e erros                                          |
| [Banco de dados](docs/database.md)  | Modelo de dados (ER), migrations e dados de exemplo                                                      |