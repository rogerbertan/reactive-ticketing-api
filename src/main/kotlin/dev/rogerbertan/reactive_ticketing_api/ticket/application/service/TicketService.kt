package dev.rogerbertan.reactive_ticketing_api.ticket.application.service

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception.TicketNotFoundException
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.TicketUseCase
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.CreateTicketCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.out.TicketRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TicketService(
    private val repository: TicketRepository
) : TicketUseCase {

    override fun getTicketsByEventId(eventId: UUID): Flow<Ticket> {
        return repository.getTicketsByEventId(eventId)
    }

    override suspend fun addTicket(
        id: UUID,
        quantity: Int
    ): Ticket {
        val ticket = repository.getTicketById(id) ?: throw TicketNotFoundException(id)
        ticket.addTickets(quantity)
        return repository.save(ticket)
    }

    override suspend fun createTicket(cmd: CreateTicketCommand): Ticket {
        return repository.save(Ticket(
            eventId = cmd.eventId,
            type = cmd.type,
            price = cmd.price,
            quantity = cmd.quantity
        ))
    }
}