package dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception

import java.util.UUID

class TicketNotFoundException(id: UUID) : RuntimeException("Ticket not found: $id")