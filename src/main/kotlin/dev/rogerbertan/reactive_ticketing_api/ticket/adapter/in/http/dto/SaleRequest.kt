package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.util.UUID

data class SaleRequest(
    @field:NotNull
    val ticketId: UUID,
    @field:NotNull
    @field:Positive
    val quantity: Int
)