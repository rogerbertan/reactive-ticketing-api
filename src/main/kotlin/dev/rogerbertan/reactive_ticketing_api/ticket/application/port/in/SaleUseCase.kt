package dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Sale
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.CreateSaleCommand

interface SaleUseCase {
    suspend fun createSale(cmd: CreateSaleCommand): Sale
}