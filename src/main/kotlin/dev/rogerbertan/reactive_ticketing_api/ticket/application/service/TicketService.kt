package dev.rogerbertan.reactive_ticketing_api.ticket.application.service

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception.TicketNotFoundException
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.TicketUseCase
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.AddTicketsCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.CreateTicketCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.SellTicketsCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.out.TicketRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TicketService(
    private val repository: TicketRepository
) : TicketUseCase {

    @Transactional(readOnly = true)
    override fun getTicketsByEventId(eventId: UUID): Flow<Ticket> {
        return repository.getTicketsByEventId(eventId)
    }

    @Transactional
    override suspend fun addTicket(cmd: AddTicketsCommand): Ticket {
        val ticket = getTicketForUpdate(cmd.ticketId)
        ticket.addTickets(cmd.quantity)
        return repository.save(ticket)
    }

    @Transactional
    override suspend fun createTicket(cmd: CreateTicketCommand): Ticket {
        return repository.save(Ticket(
            eventId = cmd.eventId,
            type = cmd.type,
            price = cmd.price,
            quantity = cmd.quantity
        ))
    }

    @Transactional
    override suspend fun sellTickets(cmd: SellTicketsCommand): Ticket {
        val ticket = getTicketForUpdate(cmd.ticketId)
        ticket.sell(cmd.quantity)
        return repository.save(ticket)
    }

    @Transactional(readOnly = true)
    override suspend fun getTicketsById(id: UUID): Ticket {
        return repository.getTicketById(id) ?: throw TicketNotFoundException(id)
    }

    private suspend fun getTicketForUpdate(id: UUID): Ticket {
        return repository.getTicketByIdForUpdate(id) ?: throw TicketNotFoundException(id)
    }
}