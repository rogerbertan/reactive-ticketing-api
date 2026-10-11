package dev.rogerbertan.reactive_ticketing_api.event.application.port.out

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.Event
import dev.rogerbertan.reactive_ticketing_api.event.application.domain.EventType
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun findAll(): Flow<Event>
    suspend fun findById(id: Long): Event?
    suspend fun save(event: Event): Event
    suspend fun deleteById(id: Long)
    fun findByType(type: EventType): Flow<Event>
}