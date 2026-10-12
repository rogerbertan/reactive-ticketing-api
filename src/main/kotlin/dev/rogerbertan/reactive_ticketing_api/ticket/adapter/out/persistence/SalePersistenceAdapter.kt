package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.out.persistence

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Sale
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.out.SaleRepository
import org.springframework.stereotype.Repository

@Repository
class SalePersistenceAdapter(
    private val repository: SaleR2dbcRepository
) : SaleRepository {

    override suspend fun save(sale: Sale): Sale {
        return repository.save(sale.toEntity()).toDomain()
    }
}