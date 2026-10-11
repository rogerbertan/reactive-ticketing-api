package dev.rogerbertan.reactive_ticketing_api.application.port.`in`

import dev.rogerbertan.reactive_ticketing_api.application.domain.Event
import dev.rogerbertan.reactive_ticketing_api.application.domain.EventType
import dev.rogerbertan.reactive_ticketing_api.application.port.`in`.command.CreateEventCommand
import kotlinx.coroutines.flow.Flow

interface EventUseCase {
    fun getAll(): Flow<Event>
    suspend fun getById(id: Long): Event
    suspend fun create(cmd: CreateEventCommand): Event
    suspend fun delete(id: Long)
    fun getByType(type: String): Flow<Event>
}