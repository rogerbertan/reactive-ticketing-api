package dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket
import kotlinx.coroutines.flow.Flow

interface TicketUseCase {
    fun getTicketsByEventId(eventId: java.util.UUID): Flow<Ticket>
}