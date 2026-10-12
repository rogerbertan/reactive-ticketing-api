package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http

import dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto.TicketResponse
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.TicketUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/tickets")
class TicketController(
    private val service: TicketUseCase
) {

    private val ticketFlow = MutableSharedFlow<Ticket>(extraBufferCapacity = 10)

    @GetMapping("/events/{eventId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getTicketsByEventId(@PathVariable eventId: UUID): Flow<TicketResponse> {
        return merge(
            service.getTicketsByEventId(eventId),
            ticketFlow.filter { it.eventId == eventId }
        ).map { it.toResponse() }
    }

    @PatchMapping("/{id}/add")
    suspend fun addTicket(@PathVariable id: UUID,
                          @RequestParam("quantity") quantity: Int)
    : TicketResponse {
        val ticket = service.addTicket(id, quantity)
        ticketFlow.emit(ticket)
        return ticket.toResponse()
    }
}