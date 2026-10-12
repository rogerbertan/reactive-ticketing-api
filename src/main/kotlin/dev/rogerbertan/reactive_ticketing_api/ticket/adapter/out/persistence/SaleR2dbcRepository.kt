package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.out.persistence

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface SaleR2dbcRepository : CoroutineCrudRepository<SaleEntity, UUID>