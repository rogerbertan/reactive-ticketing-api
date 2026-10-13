package dev.rogerbertan.reactive_ticketing_api.ticket

import dev.rogerbertan.reactive_ticketing_api.support.IntegrationTest
import dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto.SaleResponse
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.expectBody
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TicketApiIT : IntegrationTest() {

    private fun postSale(ticketId: UUID, quantity: Int) =
        webTestClient.post().uri("/v1/tickets/sale")
            .bodyValue(mapOf("ticketId" to ticketId, "quantity" to quantity))
            .exchange()

    @Test
    fun `a sale decrements the stored stock and registers the sale with its total`() = runBlocking {
        val ticket = createTicket(createEvent().id!!, price = "49.90", quantity = 10)

        val response = postSale(ticket.id!!, 3)
            .expectStatus().isCreated
            .expectBody<SaleResponse>()
            .returnResult().responseBody!!

        assertEquals(BigDecimal("149.70"), response.totalPrice)
        assertEquals(7, stockOf(ticket.id))
        assertEquals(listOf(BigDecimal("149.70")), salesTotalsOf(ticket.id))
    }

    @Test
    fun `a sale above the stock is rejected and changes nothing`() = runBlocking {
        val ticket = createTicket(createEvent().id!!, price = "10.00", quantity = 2)

        postSale(ticket.id!!, 3)
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody().jsonPath("$.title").isEqualTo("Insufficient Tickets")

        assertEquals(2, stockOf(ticket.id))
        assertTrue(salesTotalsOf(ticket.id).isEmpty())
    }

    @Test
    fun `a sale with zero quantity is rejected and changes nothing`() = runBlocking {
        val ticket = createTicket(createEvent().id!!, price = "10.00", quantity = 2)

        postSale(ticket.id!!, 0).expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)

        assertEquals(2, stockOf(ticket.id))
        assertTrue(salesTotalsOf(ticket.id).isEmpty())
    }

    @Test
    fun `a sale of a missing ticket returns not found`() {
        postSale(UUID.randomUUID(), 1)
            .expectStatus().isNotFound
            .expectBody().jsonPath("$.title").isEqualTo("Ticket Not Found")
    }

    @Test
    fun `adding zero tickets is rejected and keeps the stock`() = runBlocking {
        val ticket = createTicket(createEvent().id!!, price = "10.00", quantity = 2)

        webTestClient.patch().uri("/v1/tickets/{id}/add?quantity=0", ticket.id)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody().jsonPath("$.title").isEqualTo("Invalid Ticket Quantity")

        assertEquals(2, stockOf(ticket.id!!))
    }

    @Test
    fun `creating a ticket for a missing event returns not found`() {
        webTestClient.post().uri("/v1/tickets")
            .bodyValue(
                mapOf(
                    "eventId" to UUID.randomUUID(),
                    "type" to "VIP",
                    "price" to 100.00,
                    "quantity" to 10
                )
            )
            .exchange()
            .expectStatus().isNotFound
            .expectBody().jsonPath("$.title").isEqualTo("Event Not Found")
    }
}