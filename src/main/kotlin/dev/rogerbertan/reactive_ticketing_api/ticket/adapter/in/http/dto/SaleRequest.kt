package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto

import java.util.UUID

data class SaleRequest(
    val ticketId: UUID,
    val quantity: Int
)
