# Referência da API

[← Voltar ao README](../README.md)

URL base: `http://localhost:8080`

Todos os payloads são JSON. Datas usam o formato `YYYY-MM-DD`, IDs são UUID e valores monetários são decimais.

Os exemplos usam IDs dos [dados de exemplo](database.md#dados-de-exemplo) inseridos pelas migrations.

## Enums

| Enum         | Valores                                              |
|--------------|------------------------------------------------------|
| `EventType`  | `SHOW`, `CONCERTO`, `TEATRO`, `PALESTRA`, `WORKSHOP` |
| `TicketType` | `VIP`, `REGULAR`, `STUDENT`                          |

## Erros

Erros de negócio seguem o formato [ProblemDetail (RFC 9457)](https://www.rfc-editor.org/rfc/rfc9457):

```json
{
  "type": "about:blank",
  "title": "Insufficient Tickets",
  "status": 400,
  "detail": "Insufficient tickets available",
  "instance": "/v1/tickets/sale"
}
```

| Situação                              | Status | `title`                |
|---------------------------------------|--------|------------------------|
| Evento não encontrado                 | `404`  | `Event Not Found`      |
| Ingresso não encontrado               | `404`  | `Ticket Not Found`     |
| Estoque insuficiente para a venda     | `400`  | `Insufficient Tickets` |
| Falha de validação do body (`@Valid`) | `400`  | `Bad Request`          |

---

## Eventos

### Listar eventos

`GET /v1/events`

```bash
curl http://localhost:8080/v1/events
```

Resposta `200 OK`:

```json
[
  {
    "id": "3fdfdfd5-f757-495a-a346-a62c1347b919",
    "type": "SHOW",
    "name": "Taylor Swift",
    "date": "2024-02-15",
    "description": "Um evento imperdível para todos os amantes da música pop."
  }
]
```

### Buscar evento por ID

`GET /v1/events/{id}`

```bash
curl http://localhost:8080/v1/events/3fdfdfd5-f757-495a-a346-a62c1347b919
```

Respostas: `200 OK` com o evento, ou `404` se não existir.

### Criar evento

`POST /v1/events`

| Campo         | Tipo        | Obrigatório | Regras                        |
|---------------|-------------|-------------|-------------------------------|
| `type`        | `EventType` | sim         | um dos valores do enum        |
| `name`        | string      | sim         | não vazio, até 100 caracteres |
| `date`        | date        | não         | `YYYY-MM-DD`                  |
| `description` | string      | sim         | não vazio, até 200 caracteres |

```bash
curl -X POST http://localhost:8080/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "type": "SHOW",
    "name": "Coldplay",
    "date": "2026-12-10",
    "description": "Turnê Music of the Spheres"
  }'
```

Resposta `201 Created` com o evento criado (incluindo o `id` gerado). O evento também é enviado a quem estiver conectado no stream de categorias.

### Remover evento

`DELETE /v1/events/{id}`

```bash
curl -X DELETE http://localhost:8080/v1/events/8688ca74-0cf2-41cf-8a7c-454f1af5ca16
```

Respostas: `204 No Content`, ou `404` se não existir. Os ingressos e vendas do evento são removidos em cascata pelo banco.

### Stream de eventos por categoria (SSE)

`GET /v1/events/categories/{type}` — `text/event-stream`

`{type}` aceita qualquer capitalização (`show`, `SHOW`, `Show`).

```bash
curl -N http://localhost:8080/v1/events/categories/show
```

Saída (um item a cada 4 segundos; a conexão fica aberta):

```
data:{"id":"3fdfdfd5-...","type":"SHOW","name":"Taylor Swift","date":"2024-02-15","description":"..."}

data:{"id":"8ff50feb-...","type":"SHOW","name":"Stand-up Comedy","date":"2024-03-18","description":"..."}
```

Eventos criados via `POST /v1/events` enquanto a conexão está aberta também são enviados (de qualquer categoria). Uma categoria inexistente gera erro, pois não há tratamento específico para ela.

---

## Ingressos

### Stream de ingressos de um evento (SSE)

`GET /v1/events/{eventId}/tickets` — `text/event-stream`

```bash
curl -N http://localhost:8080/v1/events/3fdfdfd5-f757-495a-a346-a62c1347b919/tickets
```

Saída:

```
data:{"id":"<uuid>","type":"VIP","price":1200.00,"quantity":3}

data:{"id":"<uuid>","type":"REGULAR","price":650.00,"quantity":8}

data:{"id":"<uuid>","type":"STUDENT","price":325.00,"quantity":5}
```

Depois do estado inicial, a conexão continua aberta e recebe o ingresso atualizado sempre que um ingresso **desse evento** é criado, reposto ou vendido.

> Os IDs dos ingressos são gerados pelo banco. Use este endpoint para descobri-los antes de vender ou repor.

### Criar ingresso

`POST /v1/tickets`

| Campo      | Tipo         | Obrigatório | Regras                   |
|------------|--------------|-------------|--------------------------|
| `eventId`  | UUID         | sim         | evento deve existir (FK) |
| `type`     | `TicketType` | sim         | um dos valores do enum   |
| `price`    | decimal      | sim         | maior que zero           |
| `quantity` | int          | sim         | maior que zero           |

```bash
curl -X POST http://localhost:8080/v1/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "60a46af3-2eef-48c7-b2f1-87a0b9e7bf52",
    "type": "VIP",
    "price": 250.00,
    "quantity": 10
  }'
```

Resposta `201 Created`:

```json
{ "id": "<uuid>", "type": "VIP", "price": 250.00, "quantity": 10 }
```

### Repor ingressos

`PATCH /v1/tickets/{id}/add?quantity={n}`

```bash
curl -X PATCH "http://localhost:8080/v1/tickets/<id-do-ingresso>/add?quantity=5"
```

Respostas: `200 OK` com o ingresso atualizado, ou `404` se o ingresso não existir.

---

## Vendas

### Vender ingressos

`POST /v1/tickets/sale`

| Campo      | Tipo | Obrigatório |
|------------|------|-------------|
| `ticketId` | UUID | sim         |
| `quantity` | int  | sim         |

```bash
curl -X POST http://localhost:8080/v1/tickets/sale \
  -H "Content-Type: application/json" \
  -d '{ "ticketId": "<id-do-ingresso>", "quantity": 2 }'
```

Resposta `201 Created`:

```json
{
  "id": "<uuid-da-venda>",
  "ticketId": "<id-do-ingresso>",
  "quantity": 2,
  "totalPrice": 2400.00
}
```

`totalPrice` = preço do ingresso × quantidade.

Erros:

- `404 Ticket Not Found` — ingresso não existe
- `400 Insufficient Tickets` — quantidade maior que o estoque disponível

Como a venda funciona por dentro (transação, lock e notificação SSE) está em [Fluxos](flows.md#venda-de-ingressos).