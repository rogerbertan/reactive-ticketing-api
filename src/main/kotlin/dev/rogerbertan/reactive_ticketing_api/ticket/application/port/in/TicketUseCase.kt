package dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.AddTicketsCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.CreateTicketCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.SellTicketsCommand
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface TicketUseCase {
    fun getTicketsByEventId(eventId: java.util.UUID): Flow<Ticket>
    suspend fun addTicket(cmd: AddTicketsCommand): Ticket
    suspend fun createTicket(cmd: CreateTicketCommand): Ticket
    suspend fun getTicketsById(id: UUID): Ticket
    suspend fun sellTickets(cmd: SellTicketsCommand): Ticket
}