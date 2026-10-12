package dev.rogerbertan.reactive_ticketing_api.shared.adapter.`in`.http

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.exception.EventNotFoundException
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception.InsufficientTicketsException
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception.TicketNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EventNotFoundException::class)
    fun handleEventNotFoundException(ex: EventNotFoundException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.message
        )
        problem.title = "Event Not Found"
        return problem
    }

    @ExceptionHandler(TicketNotFoundException::class)
    fun handleTicketNotFoundException(ex: TicketNotFoundException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.message
        )
        problem.title = "Ticket Not Found"
        return problem
    }

    @ExceptionHandler(InsufficientTicketsException::class)
    fun handleInsufficientTicketsException(ex: InsufficientTicketsException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.message
        )
        problem.title = "Insufficient Tickets"
        return problem
    }
}