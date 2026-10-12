package dev.rogerbertan.reactive_ticketing_api.ticket.application.domain

import java.math.BigDecimal
import java.util.UUID

data class Sale(
    val id: UUID?,
    val ticketId: UUID,
    val quantity: Int,
    val totalPrice: BigDecimal
)