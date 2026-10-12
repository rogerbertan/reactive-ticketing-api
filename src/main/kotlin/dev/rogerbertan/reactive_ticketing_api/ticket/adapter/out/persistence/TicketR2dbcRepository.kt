package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.out.persistence

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface TicketR2dbcRepository : CoroutineCrudRepository<TicketEntity, UUID> {
    fun getTicketsByEventId(eventId: UUID): Flow<TicketEntity>

    @Query("SELECT * FROM tickets WHERE id = :id FOR UPDATE")
    suspend fun findByIdForUpdate(id: UUID): TicketEntity?
}