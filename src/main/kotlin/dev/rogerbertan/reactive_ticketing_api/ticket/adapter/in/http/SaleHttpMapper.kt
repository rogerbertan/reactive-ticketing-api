package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http

import dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto.SaleRequest
import dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto.SaleResponse
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Sale
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.CreateSaleCommand

fun SaleRequest.toCommand(): CreateSaleCommand {
    return CreateSaleCommand(
        ticketId = ticketId,
        quantity = quantity
    )
}

fun Sale.toResponse(): SaleResponse {
    return SaleResponse(
        id = id,
        ticketId = ticketId,
        quantity = quantity,
        totalPrice = totalPrice
    )
}