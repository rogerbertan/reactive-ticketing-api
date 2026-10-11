package dev.rogerbertan.reactive_ticketing_api.application.port.`in`

import dev.rogerbertan.reactive_ticketing_api.application.domain.Event
import kotlinx.coroutines.flow.Flow

interface EventUseCase {
    fun getAll(): Flow<Event>
}