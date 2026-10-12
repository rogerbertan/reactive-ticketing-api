package dev.rogerbertan.reactive_ticketing_api.event.application.port.`in`

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.Event
import dev.rogerbertan.reactive_ticketing_api.event.application.domain.EventType
import dev.rogerbertan.reactive_ticketing_api.event.application.port.`in`.command.CreateEventCommand
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface EventUseCase {
    fun getAll(): Flow<Event>
    suspend fun getById(id: UUID): Event
    suspend fun create(cmd: CreateEventCommand): Event
    suspend fun delete(id: UUID)
    fun getByType(type: String): Flow<Event>
}