package dev.rogerbertan.reactive_ticketing_api.ticket.application.domain

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception.InsufficientTicketsException
import java.math.BigDecimal
import java.util.UUID

data class Ticket(
    val id: UUID? = null,
    val eventId: UUID,
    val type: TicketType,
    val price: BigDecimal,
    var quantity: Int,
) {
    fun addTickets(quantity: Int) {
        this.quantity += quantity
    }

    fun sell(quantity: Int) {
        if (quantity > this.quantity) {
            throw InsufficientTicketsException()
        }
        this.quantity -= quantity
    }
}
