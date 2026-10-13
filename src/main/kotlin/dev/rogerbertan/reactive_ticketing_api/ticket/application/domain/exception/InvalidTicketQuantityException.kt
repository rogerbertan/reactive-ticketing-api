package dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception

class InvalidTicketQuantityException : RuntimeException("Ticket quantity must be greater than zero")