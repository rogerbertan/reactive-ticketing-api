package dev.rogerbertan.reactive_ticketing_api.application.port.out

import dev.rogerbertan.reactive_ticketing_api.application.domain.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun findAll(): Flow<Event>
    suspend fun findById(id: Long): Event?
    suspend fun save(event: Event): Event
}