# Testes

[← Voltar ao README](../README.md)

A estratégia é testar **comportamento com valor**, não volume: cada teste existe para proteger uma regra de negócio, um contrato da API ou uma correção que já aconteceu (regressão). Classes sem comportamento (DTOs, commands, mappers, exceptions, entities, enums) não têm testes próprios.

## Stack

| Ferramenta                     | Uso                                                                                     |
|--------------------------------|-----------------------------------------------------------------------------------------|
| JUnit 5                        | Execução dos testes, testes parametrizados (`@ParameterizedTest`)                       |
| MockK                          | Mocks com suporte a `suspend` (`coEvery`, `coVerify`, `slot`, `coVerifyOrder`)          |
| kotlinx-coroutines-test        | `runTest` e `backgroundScope` para testar código `suspend` e `Flow`                     |
| Testcontainers (PostgreSQL 16) | Banco real em Docker para os testes de integração                                       |
| WebTestClient                  | Chamadas HTTP reais contra a aplicação em porta aleatória                               |
| PIT (pitest)                   | Testes de mutação: verifica se os testes unitários detectam defeitos introduzidos       |
| JaCoCo                         | Relatório de cobertura de linhas e branches (unitários + integração)                    |

## Como executar

Os testes de integração precisam do **Docker rodando** (o Testcontainers sobe o PostgreSQL sozinho; não é preciso `docker compose up`). Os testes unitários não precisam de Docker.

```bash
./gradlew test                                  # tudo: unitários + integração (gera o relatório do JaCoCo)
./gradlew test --tests '*Test'                  # só unitários
./gradlew test --tests '*IT'                    # só integração
./gradlew test --tests '*TicketConcurrencyIT'   # uma classe específica
./gradlew pitest                                # testes de mutação (só unitários)
```

Convenção de nomes: classes terminadas em `Test` são unitárias; em `IT`, de integração.

## Relatórios

| Relatório                | Caminho                                          | Gerado por                    |
|--------------------------|--------------------------------------------------|-------------------------------|
| Resultado dos testes     | `build/reports/tests/test/index.html`            | `./gradlew test`              |
| Cobertura (JaCoCo)       | `build/reports/jacoco/test/html/index.html`      | `./gradlew test` (automático) |
| Cobertura (XML, para CI) | `build/reports/jacoco/test/jacocoTestReport.xml` | `./gradlew test` (automático) |
| Mutação (PIT)            | `build/reports/pitest/index.html`                | `./gradlew pitest`            |

### Cobertura (JaCoCo)

O relatório é gerado ao final de todo `./gradlew test`. Ficam **fora** do relatório as classes sem comportamento: `dto`, `command`, `exception`, `port` (interfaces), `*MapperKt`, `*Entity`, enums (`*Type`), `Event`, `Sale`, a classe `main` e as classes `$$inlined$` que o compilador Kotlin gera para operadores de `Flow`. A lista fica em `coverageExclusions` no `build.gradle.kts`.

Se só os testes unitários forem executados, a cobertura cai, porque controllers e adapters de persistência são exercitados pelos testes de integração.

### Mutação (PIT)

O PIT altera o bytecode das classes (troca `>` por `>=`, `-=` por `+=`, remove chamadas, troca retornos) e roda os testes contra cada versão alterada (*mutante*). Se algum teste falha, o mutante foi **morto**; se todos passam, ele **sobreviveu** e indica um defeito que a suíte não detectaria.

- **Alvo:** domínio, services e `TicketController` (configurado no bloco `pitest {}` do `build.gradle.kts`).
- **Testes usados:** apenas os unitários.
- **Limite:** o build falha se o *mutation score* ficar abaixo de **80%**.

Os mutantes que sobrevivem hoje são da máquina de estados que o Kotlin gera para funções `suspend` (`throwOnFailure`), não de regra de negócio.

## Testes unitários

Ficam em `src/test/kotlin/...`, espelhando o pacote da classe testada.

| Classe testada     | O que é garantido                                                                                                                                                                                       |
|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Ticket`           | Venda subtrai o estoque; vender **exatamente** o estoque zera; vender acima lança `InsufficientTicketsException`; quantidade `<= 0` lança `InvalidTicketQuantityException`. Em erro, o estoque não muda |
| `TicketService`    | Venda e reposição leem o ingresso **com lock** (`getTicketByIdForUpdate`); ingresso ou evento inexistente → not found; nada é salvo quando a operação falha                                             |
| `SaleService`      | `totalPrice = preço × quantidade`; o estoque é baixado **antes** de a venda ser registrada; a venda não é registrada se a baixa falhar                                                                  |
| `EventService`     | Evento inexistente → `EventNotFoundException` (e o delete não apaga nada); categoria aceita qualquer capitalização; categoria inválida → `InvalidEventTypeException`                                    |
| `TicketController` | O stream SSE de um evento só recebe ingressos daquele evento; após uma venda, o stream recebe o ingresso com o estoque já atualizado                                                                    |

## Testes de integração

Herdam de `support/IntegrationTest.kt`, que:

- sobe **um único** container `postgres:16` compartilhado por todas as classes (o Flyway aplica as migrations nele);
- conecta R2DBC e Flyway ao container via `@ServiceConnection`, sem alterar o `application.yaml`;
- sobe a aplicação completa em porta aleatória (`@SpringBootTest(RANDOM_PORT)`);
- oferece helpers para criar dados próprios de cada teste (`createEvent`, `createTicket`) e conferir o banco (`stockOf`, `salesTotalsOf`). Os testes não dependem dos dados de exemplo das migrations.

| Classe                | O que é garantido                                                                                                                                                                                                                                                    |
|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `TicketApiIT`         | Venda grava o estoque e a linha em `sales` com o total correto; venda acima do estoque, com quantidade 0 ou de ingresso inexistente devolve o erro certo **sem alterar o banco**; reposição com quantidade 0 → `400`; criar ingresso para evento inexistente → `404` |
| `TicketConcurrencyIT` | 10 vendas simultâneas com estoque 5 resultam em exatamente 5 vendas e estoque 0 (lock `FOR UPDATE`)                                                                                                                                                                  |
| `SaleAtomicityIT`     | Se o registro da venda falha, a baixa de estoque é desfeita (`@Transactional` com coroutines/R2DBC)                                                                                                                                                                  |
| `TicketPersistenceIT` | O banco rejeita estoque negativo mesmo sem passar pelo domínio (`CHECK`); a consulta de ingressos por evento não traz ingressos de outros eventos                                                                                                                    |
| `EventIT`             | Tipo e data do evento são persistidos e lidos corretamente (inclusive data `null`); categoria inválida → `400`; remover um evento remove ingressos e vendas em cascata                                                                                               |

## Testes de regressão

Correções que têm um teste dedicado para não voltarem:

| Correção                                                | Testes                                                                               |
|---------------------------------------------------------|--------------------------------------------------------------------------------------|
| Lock da linha do ingresso na venda e na reposição       | `TicketServiceTest` (usa o lock), `TicketConcurrencyIT` (sem venda acima do estoque) |
| Estoque nunca negativo no banco (`CHECK quantity >= 0`) | `TicketPersistenceIT`                                                                |
| Venda/reposição com quantidade zero ou negativa         | `TicketTest`, `TicketApiIT`                                                          |
| Ingresso para evento inexistente retornava `500`        | `TicketServiceTest`, `TicketApiIT`                                                   |
| Categoria inválida retornava `500`                      | `EventServiceTest`, `EventIT`                                                        |