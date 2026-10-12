package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.out.persistence

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Sale

fun Sale.toEntity(): SaleEntity {
    return SaleEntity(
        id = id,
        ticketId = ticketId,
        quantity = quantity,
        totalPrice = totalPrice,
    )
}

fun SaleEntity.toDomain(): Sale {
    return Sale(
        id = id,
        ticketId = ticketId,
        quantity = quantity,
        totalPrice = totalPrice,
    )
}