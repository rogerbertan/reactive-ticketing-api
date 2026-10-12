package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http

import dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto.TicketRequest
import dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto.TicketResponse
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.AddTicketsCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.CreateTicketCommand
import java.util.UUID

fun Ticket.toResponse(): TicketResponse {
    return TicketResponse(
        id = id,
        type = type,
        price = price,
        quantity = quantity
    )
}

fun toAddTicketsCommand(ticketId: UUID, quantity: Int): AddTicketsCommand {
    return AddTicketsCommand(
        ticketId = ticketId,
        quantity = quantity
    )
}

fun TicketRequest.toCommand(): CreateTicketCommand {
    return CreateTicketCommand(
        eventId = eventId,
        type = type,
        price = price,
        quantity = quantity
    )
}