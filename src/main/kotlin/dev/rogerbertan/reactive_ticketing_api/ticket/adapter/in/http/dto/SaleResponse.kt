package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto

import java.math.BigDecimal
import java.util.UUID

data class SaleResponse(
    val id: UUID?,
    val ticketId: UUID,
    val quantity: Int,
    val totalPrice: BigDecimal
)
