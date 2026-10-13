package dev.rogerbertan.reactive_ticketing_api.ticket.application.domain

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception.InsufficientTicketsException
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception.InvalidTicketQuantityException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals

class TicketTest {

    private fun ticketWithStock(quantity: Int) = Ticket(
        id = UUID.randomUUID(),
        eventId = UUID.randomUUID(),
        type = TicketType.REGULAR,
        price = BigDecimal("50.00"),
        quantity = quantity
    )

    @Test
    fun `selling less than the stock subtracts exactly the sold quantity`() {
        val ticket = ticketWithStock(10)

        ticket.sell(3)

        assertEquals(7, ticket.quantity)
    }

    @Test
    fun `selling exactly the available stock leaves it at zero`() {
        val ticket = ticketWithStock(5)

        ticket.sell(5)

        assertEquals(0, ticket.quantity)
    }

    @Test
    fun `selling more than the available stock is rejected without changing the stock`() {
        val ticket = ticketWithStock(5)

        assertThrows<InsufficientTicketsException> { ticket.sell(6) }
        assertEquals(5, ticket.quantity)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1, -5])
    fun `selling a non positive quantity is rejected without changing the stock`(quantity: Int) {
        val ticket = ticketWithStock(5)

        assertThrows<InvalidTicketQuantityException> { ticket.sell(quantity) }
        assertEquals(5, ticket.quantity)
    }

    @Test
    fun `adding tickets increases the stock by the added quantity`() {
        val ticket = ticketWithStock(5)

        ticket.addTickets(3)

        assertEquals(8, ticket.quantity)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1, -10])
    fun `adding a non positive quantity is rejected without changing the stock`(quantity: Int) {
        val ticket = ticketWithStock(5)

        assertThrows<InvalidTicketQuantityException> { ticket.addTickets(quantity) }
        assertEquals(5, ticket.quantity)
    }
}