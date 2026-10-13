# Arquitetura

[← Voltar ao README](../README.md)

## Visão geral

A aplicação é um único serviço Spring Boot reativo (WebFlux + Kotlin Coroutines) que persiste dados em um PostgreSQL via R2DBC. O schema do banco é versionado com Flyway.

O código segue **arquitetura hexagonal (ports & adapters)**: o núcleo da aplicação (domínio e casos de uso) não conhece HTTP nem banco de dados. Ele expõe **portas de entrada** (interfaces de caso de uso) e depende de **portas de saída** (interfaces de repositório). Os **adapters** fazem a ponte com o mundo externo.

## Diagramas C4

### Nível 1 — Contexto

```mermaid
flowchart LR
    client(["👤 Cliente<br/>(front-end, curl, Postman)"])
    api["Reactive Ticketing API<br/><i>Cadastro de eventos e venda de ingressos</i>"]
    db[("PostgreSQL")]

    client -- "HTTP/JSON e SSE" --> api
    api -- "Lê e grava eventos,<br/>ingressos e vendas" --> db
```

### Nível 2 — Contêineres

```mermaid
flowchart LR
    client(["👤 Cliente"])

    subgraph app["Aplicação Spring Boot (JVM 21)"]
        web["Spring WebFlux<br/><i>Servidor HTTP não bloqueante (Netty)</i>"]
        core["Núcleo da aplicação<br/><i>Domínio + casos de uso (Coroutines)</i>"]
        r2dbc["Spring Data R2DBC<br/><i>Driver reativo r2dbc-postgresql</i>"]
        flyway["Flyway<br/><i>Migrations na inicialização (JDBC)</i>"]
        web --> core --> r2dbc
    end

    db[("PostgreSQL 16<br/><i>Docker Compose</i>")]

    client -- "HTTP :8080" --> web
    r2dbc -- "R2DBC :5432" --> db
    flyway -- "JDBC :5432" --> db
```

O Flyway não tem suporte a R2DBC, por isso usa uma conexão JDBC separada apenas para rodar as migrations (configurada em `spring.flyway.*` no `application.yaml`). Todo o resto do acesso ao banco é reativo.

### Nível 3 — Componentes

```mermaid
flowchart TB
    client(["👤 Cliente"])

    subgraph in["Adapters de entrada (HTTP)"]
        ec["EventController<br/>/v1/events"]
        tc["TicketController<br/>/v1/tickets, /v1/events/{id}/tickets"]
        geh["GlobalExceptionHandler<br/><i>exceções → ProblemDetail</i>"]
    end

    subgraph core["Núcleo da aplicação"]
        euc{{"EventUseCase"}}
        tuc{{"TicketUseCase"}}
        suc{{"SaleUseCase"}}
        es["EventService"]
        ts["TicketService"]
        ss["SaleService"]
        er{{"EventRepository"}}
        tr{{"TicketRepository"}}
        sr{{"SaleRepository"}}
    end

    subgraph out["Adapters de saída (persistência)"]
        epa["EventPersistenceAdapter"]
        tpa["TicketPersistenceAdapter"]
        spa["SalePersistenceAdapter"]
    end

    db[("PostgreSQL")]

    client --> ec & tc
    ec --> euc
    tc --> tuc & suc
    euc -. implementado por .-> es
    tuc -. implementado por .-> ts
    suc -. implementado por .-> ss
    ss --> tuc
    ts --> euc
    es --> er
    ts --> tr
    ss --> sr
    er -. implementado por .-> epa
    tr -. implementado por .-> tpa
    sr -. implementado por .-> spa
    epa & tpa & spa --> db
```

Hexágonos (`{{ }}`) são portas (interfaces); retângulos são implementações. O `SaleService` depende do `TicketUseCase` (e não do repositório de tickets) para reaproveitar a regra de baixa de estoque. Da mesma forma, o `TicketService` usa o `EventUseCase` para validar que o evento existe antes de criar um ingresso.

## Camadas e responsabilidades

Cada módulo (`event`, `ticket`) tem a mesma estrutura dentro de `src/main/kotlin/dev/rogerbertan/reactive_ticketing_api/`:

| Camada             | Pacote                    | Responsabilidade                                                            | Exemplos                                                               |
|--------------------|---------------------------|-----------------------------------------------------------------------------|------------------------------------------------------------------------|
| Domínio            | `application/domain`      | Modelos e regras de negócio puras, exceções de domínio                      | `Ticket.sell()`, `Ticket.addTickets()`, `InsufficientTicketsException` |
| Porta de entrada   | `application/port/in`     | Interfaces dos casos de uso e seus *commands* (dados de entrada)            | `TicketUseCase`, `CreateSaleCommand`                                   |
| Serviço            | `application/service`     | Implementa os casos de uso e define as transações (`@Transactional`)        | `TicketService`, `SaleService`                                         |
| Porta de saída     | `application/port/out`    | Interfaces que o núcleo usa para persistir dados                            | `TicketRepository`, `SaleRepository`                                   |
| Adapter de entrada | `adapter/in/http`         | Controllers, DTOs de request/response, validação e mapeamento DTO → command | `TicketController`, `TicketRequest`, `TicketHttpMapper.kt`             |
| Adapter de saída   | `adapter/out/persistence` | Entidades R2DBC, repositórios Spring Data e mapeamento entidade ↔ domínio   | `TicketEntity`, `TicketR2dbcRepository`, `TicketPersistenceAdapter`    |

O módulo `shared` contém o `GlobalExceptionHandler`, que converte exceções de domínio em respostas `ProblemDetail` (RFC 9457).

### Fluxo de dados entre camadas

```
Request JSON → DTO (Request) → Command → Service → Domínio → Port out → Entity → Banco
Banco → Entity → Domínio → DTO (Response) → JSON
```

O mapeamento é feito com *extension functions* Kotlin (`toCommand()`, `toResponse()`, `toDomain()`, `toEntity()`), mantendo as camadas desacopladas.

## Modelo reativo

- **WebFlux + Coroutines:** os controllers usam funções `suspend` (respostas únicas) e `Flow<T>` (streams). O Spring adapta isso automaticamente para `Mono`/`Flux` sobre o Netty, sem bloquear threads.
- **R2DBC:** os repositórios estendem `CoroutineCrudRepository`, então leituras e escritas também são não bloqueantes.
- **Transações reativas:** `@Transactional` funciona em funções `suspend` e `Flow`, usando o `R2dbcTransactionManager`.
- **Atualizações em tempo real (SSE):** cada controller mantém um `MutableSharedFlow` em memória. Quando algo muda (evento criado, ingresso criado/reposto/vendido), o controller emite o novo estado nesse flow. Os endpoints SSE fazem `merge` entre os dados atuais do banco e esse flow, então quem está conectado recebe as mudanças à medida que acontecem. Detalhes em [Fluxos](flows.md).

> Como o `MutableSharedFlow` vive na memória da instância, as atualizações SSE só chegam a clientes conectados à mesma instância da aplicação. Para rodar várias instâncias seria necessário um broker (ex.: Redis Pub/Sub, Kafka ou `LISTEN/NOTIFY` do PostgreSQL).