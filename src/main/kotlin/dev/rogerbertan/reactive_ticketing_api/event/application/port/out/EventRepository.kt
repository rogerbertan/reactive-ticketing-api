package dev.rogerbertan.reactive_ticketing_api.event.application.port.out

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.Event
import dev.rogerbertan.reactive_ticketing_api.event.application.domain.EventType
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface EventRepository {
    fun findAll(): Flow<Event>
    suspend fun findById(id: UUID): Event?
    suspend fun save(event: Event): Event
    suspend fun deleteById(id: UUID)
    fun findByType(type: EventType): Flow<Event>
}