package dev.rogerbertan.reactive_ticketing_api.support

import dev.rogerbertan.reactive_ticketing_api.event.adapter.out.persistence.EventEntity
import dev.rogerbertan.reactive_ticketing_api.event.adapter.out.persistence.EventR2dbcRepository
import dev.rogerbertan.reactive_ticketing_api.event.application.domain.EventType
import dev.rogerbertan.reactive_ticketing_api.ticket.adapter.out.persistence.TicketEntity
import dev.rogerbertan.reactive_ticketing_api.ticket.adapter.out.persistence.TicketR2dbcRepository
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.TicketType
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.postgresql.PostgreSQLContainer
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
abstract class IntegrationTest {

    companion object {
        // Started once and shared by every Spring context; Ryuk removes it when the JVM exits.
        @JvmField
        @ServiceConnection
        val postgres: PostgreSQLContainer = PostgreSQLContainer("postgres:16").apply { start() }
    }

    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    protected lateinit var eventRepository: EventR2dbcRepository

    @Autowired
    protected lateinit var ticketRepository: TicketR2dbcRepository

    @Autowired
    protected lateinit var databaseClient: DatabaseClient

    protected suspend fun createEvent(): EventEntity {
        return eventRepository.save(
            EventEntity(
                type = EventType.SHOW,
                name = "Integration test event",
                date = LocalDate.of(2026, 12, 1),
                description = "Created by an integration test"
            )
        )
    }

    protected suspend fun createTicket(eventId: UUID, price: String, quantity: Int): TicketEntity {
        return ticketRepository.save(
            TicketEntity(
                id = null,
                eventId = eventId,
                type = TicketType.REGULAR,
                price = BigDecimal(price),
                quantity = quantity
            )
        )
    }

    protected suspend fun stockOf(ticketId: UUID): Int {
        return ticketRepository.findById(ticketId)!!.quantity
    }

    protected suspend fun salesTotalsOf(ticketId: UUID): List<BigDecimal> {
        return databaseClient.sql("SELECT total_price FROM sales WHERE ticket_id = :ticketId")
            .bind("ticketId", ticketId)
            .map { row, _ -> row.get("total_price", BigDecimal::class.java)!! }
            .all()
            .collectList()
            .awaitSingle()
    }
}