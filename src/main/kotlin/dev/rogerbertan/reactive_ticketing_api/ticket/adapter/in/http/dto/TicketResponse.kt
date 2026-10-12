package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.TicketType
import java.math.BigDecimal
import java.util.UUID

data class TicketResponse(
    val id: UUID?,
    val type: TicketType,
    val price: BigDecimal,
    val quantity: Int
)
