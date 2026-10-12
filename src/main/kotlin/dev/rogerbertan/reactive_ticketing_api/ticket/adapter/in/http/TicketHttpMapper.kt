package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http

import dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto.TicketResponse
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket

fun Ticket.toResponse(): TicketResponse {
    return TicketResponse(
        type = type,
        price = price,
        quantity = quantity
    )
}