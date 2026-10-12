package dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command

import java.util.UUID

data class AddTicketsCommand(
    val ticketId: UUID,
    val quantity: Int
)