package dev.rogerbertan.reactive_ticketing_api.ticket.application.port.out

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Sale

interface SaleRepository {
    suspend fun save(sale: Sale): Sale
}