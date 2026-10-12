package dev.rogerbertan.reactive_ticketing_api.ticket.application.port.out

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface TicketRepository {
    fun getTicketsByEventId(eventId: UUID): Flow<Ticket>
    suspend fun getTicketById(id: UUID): Ticket?
    suspend fun getTicketByIdForUpdate(id: UUID): Ticket?
    suspend fun save(ticket: Ticket): Ticket
}