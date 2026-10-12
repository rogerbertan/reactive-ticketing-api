# Fluxos

[← Voltar ao README](../README.md)

Diagramas de sequência dos principais fluxos da aplicação.

## Venda de ingressos

`POST /v1/tickets/sale`

A venda reduz o estoque do ingresso e registra uma linha na tabela `sales`, tudo na mesma transação. O ingresso é travado com `SELECT ... FOR UPDATE` para que vendas simultâneas não vendam o mesmo estoque duas vezes.

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant TC as TicketController
    participant SS as SaleService
    participant TS as TicketService
    participant DB as PostgreSQL
    participant F as ticketFlow (SharedFlow)
    actor S as Assinantes SSE

    C->>TC: POST /v1/tickets/sale {ticketId, quantity}
    TC->>SS: createSale(CreateSaleCommand)
    Note over SS,DB: @Transactional — início da transação
    SS->>TS: sellTickets(SellTicketsCommand)
    TS->>DB: SELECT * FROM tickets WHERE id = ? FOR UPDATE
    alt ingresso não existe
        DB-->>TS: vazio
        TS-->>C: 404 Ticket Not Found
    end
    DB-->>TS: ticket (linha travada)
    TS->>TS: ticket.sell(quantity)
    alt quantity > estoque
        TS-->>C: 400 Insufficient Tickets (rollback)
    end
    TS->>DB: UPDATE tickets SET quantity = quantity - n
    TS-->>SS: ticket atualizado
    SS->>SS: totalPrice = price × quantity
    SS->>DB: INSERT INTO sales
    Note over SS,DB: commit — lock liberado
    SS-->>TC: Sale
    TC->>TS: getTicketsById(ticketId)
    TS->>DB: SELECT ticket
    TC->>F: emit(ticket)
    F-->>S: ingresso com estoque atualizado
    TC-->>C: 201 Created {id, ticketId, quantity, totalPrice}
```

## Reposição de ingressos

`PATCH /v1/tickets/{id}/add?quantity=N`

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant TC as TicketController
    participant TS as TicketService
    participant DB as PostgreSQL
    participant F as ticketFlow
    actor S as Assinantes SSE

    C->>TC: PATCH /v1/tickets/{id}/add?quantity=N
    TC->>TS: addTicket(AddTicketsCommand)
    Note over TS,DB: @Transactional
    TS->>DB: SELECT ... FOR UPDATE
    DB-->>TS: ticket
    TS->>TS: ticket.addTickets(N)
    TS->>DB: UPDATE tickets
    TS-->>TC: ticket atualizado
    TC->>F: emit(ticket)
    F-->>S: ingresso com estoque atualizado
    TC-->>C: 200 OK {id, type, price, quantity}
```

A criação de um novo tipo de ingresso (`POST /v1/tickets`) segue o mesmo padrão: salva no banco e emite o ingresso criado no `ticketFlow`.

## Stream de ingressos de um evento (SSE)

`GET /v1/events/{eventId}/tickets` — `Content-Type: text/event-stream`

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente SSE
    participant TC as TicketController
    participant TS as TicketService
    participant DB as PostgreSQL
    participant F as ticketFlow

    C->>TC: GET /v1/events/{eventId}/tickets
    TC->>TS: getTicketsByEventId(eventId)
    TS->>DB: SELECT * FROM tickets WHERE event_id = ?
    DB-->>C: data: {ingresso 1}
    DB-->>C: data: {ingresso 2}
    Note over C,F: conexão continua aberta
    loop a cada criação, reposição ou venda
        F-->>TC: ticket alterado
        TC->>TC: filter(ticket.eventId == eventId)
        TC-->>C: data: {ingresso atualizado}
    end
```

O resultado é um `merge` entre o snapshot atual do banco e o `ticketFlow` filtrado pelo evento. O cliente recebe primeiro o estado atual e depois cada mudança.

## Stream de eventos por categoria (SSE)

`GET /v1/events/categories/{type}` — `Content-Type: text/event-stream`

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente SSE
    participant EC as EventController
    participant ES as EventService
    participant DB as PostgreSQL
    participant F as eventFlow
    actor A as Outro cliente

    C->>EC: GET /v1/events/categories/show
    EC->>ES: getByType("show")
    ES->>ES: EventType.valueOf("SHOW")
    ES->>DB: SELECT * FROM events WHERE type = 'SHOW'
    loop para cada evento (intervalo de 4s)
        EC-->>C: data: {evento}
    end
    A->>EC: POST /v1/events
    EC->>F: emit(evento criado)
    F-->>EC: evento criado
    EC-->>C: data: {evento criado} (após 4s)
```

Pontos de atenção:

- Existe um `delay` de 4 segundos antes de cada item, para deixar o comportamento de streaming visível.
- O `eventFlow` **não** é filtrado por categoria: quem está assinando `/categories/show` também recebe eventos novos de outras categorias.

## Controle de concorrência

Duas vendas simultâneas do mesmo ingresso poderiam ler o mesmo estoque e as duas "passarem" na validação. A aplicação evita isso com duas camadas:

1. **Lock pessimista na linha:** `TicketR2dbcRepository.findByIdForUpdate` executa `SELECT * FROM tickets WHERE id = :id FOR UPDATE` dentro da transação. A segunda transação espera a primeira terminar e só então lê o estoque já atualizado. Vale para venda e reposição.
2. **Constraint no banco:** a coluna `tickets.quantity` tem `check (quantity >= 0)`. Mesmo que algum caminho de código escape da validação, o banco rejeita estoque negativo.

```mermaid
sequenceDiagram
    participant T1 as Venda A (2 ingressos)
    participant DB as tickets (estoque = 3)
    participant T2 as Venda B (2 ingressos)

    T1->>DB: SELECT ... FOR UPDATE
    DB-->>T1: quantity = 3 (lock)
    T2->>DB: SELECT ... FOR UPDATE
    Note over T2,DB: aguardando o lock de A
    T1->>DB: UPDATE quantity = 1 + INSERT sale
    T1->>DB: COMMIT (libera o lock)
    DB-->>T2: quantity = 1
    T2->>T2: 2 > 1 → InsufficientTicketsException
    T2->>DB: ROLLBACK
```