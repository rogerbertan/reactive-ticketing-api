package dev.rogerbertan.reactive_ticketing_api.application.service

import dev.rogerbertan.reactive_ticketing_api.application.domain.Event
import dev.rogerbertan.reactive_ticketing_api.application.port.`in`.EventUseCase
import dev.rogerbertan.reactive_ticketing_api.application.port.out.EventRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service

@Service
class EventService(
    private val repository: EventRepository
) : EventUseCase {

    override fun getAll(): Flow<Event> {
        return repository.findAll()
    }
}