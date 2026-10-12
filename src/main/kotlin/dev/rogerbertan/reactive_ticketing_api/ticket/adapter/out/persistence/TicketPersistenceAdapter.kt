package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.out.persistence

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.out.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class TicketPersistenceAdapter(
    private val repository: TicketR2dbcRepository
) : TicketRepository {

    override fun getTicketsByEventId(eventId: UUID): Flow<Ticket> {
        return repository.getTicketsByEventId(eventId).map { it.toDomain() }
    }

    override suspend fun getTicketById(id: UUID): Ticket? {
        return repository.findById(id)?.toDomain()
    }

    override suspend fun save(ticket: Ticket): Ticket {
        return repository.save(ticket.toEntity()).toDomain()
    }
}