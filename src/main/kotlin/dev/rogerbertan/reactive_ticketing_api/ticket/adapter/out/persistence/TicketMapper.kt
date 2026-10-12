package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.out.persistence

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket

fun TicketEntity.toDomain(): Ticket {
    return Ticket(
        id = id,
        eventId = eventId,
        type = type,
        price = price,
        quantity = quantity,
    )
}

fun Ticket.toEntity(): TicketEntity {
    return TicketEntity(
        id = id,
        eventId = eventId,
        type = type,
        price = price,
        quantity = quantity,
    )
}