package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http

import dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto.TicketRequest
import dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto.TicketResponse
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.TicketUseCase
import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1")
class TicketController(
    private val service: TicketUseCase
) {

    private val ticketFlow = MutableSharedFlow<Ticket>(extraBufferCapacity = 10)

    @GetMapping("/events/{eventId}/tickets", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getTicketsByEventId(@PathVariable eventId: UUID): Flow<TicketResponse> {
        return merge(
            service.getTicketsByEventId(eventId),
            ticketFlow.filter { it.eventId == eventId }
        ).map { it.toResponse() }
    }

    @PatchMapping("tickets/{id}/add")
    suspend fun addTicket(@PathVariable id: UUID,
                          @RequestParam("quantity") quantity: Int)
    : TicketResponse {
        val ticket = service.addTicket(id, quantity)
        ticketFlow.emit(ticket)
        return ticket.toResponse()
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createTicket(@Valid @RequestBody request: TicketRequest): TicketResponse {
        val ticket = service.createTicket(request.toCommand())
        ticketFlow.emit(ticket)
        return ticket.toResponse()
    }
}