package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.TicketType
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

data class TicketRequest(
    @field:NotNull
    val eventId: UUID,
    @field:NotNull
    val type: TicketType,
    @field:NotNull
    @field:Positive
    val price: BigDecimal,
    @field:NotNull
    @field:Positive
    val quantity: Int
)
