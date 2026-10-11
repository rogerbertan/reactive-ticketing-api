package dev.rogerbertan.reactive_ticketing_api.application.service

import dev.rogerbertan.reactive_ticketing_api.application.domain.Event
import dev.rogerbertan.reactive_ticketing_api.application.domain.EventType
import dev.rogerbertan.reactive_ticketing_api.application.domain.exception.EventNotFoundException
import dev.rogerbertan.reactive_ticketing_api.application.port.`in`.EventUseCase
import dev.rogerbertan.reactive_ticketing_api.application.port.`in`.command.CreateEventCommand
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

    override suspend fun getById(id: Long): Event {
        return repository.findById(id) ?: throw EventNotFoundException(id)
    }

    override suspend fun create(cmd: CreateEventCommand): Event {
        return repository.save(Event(
            type = cmd.type,
            name = cmd.name,
            date = cmd.date,
            description = cmd.description
        ))
    }

    override suspend fun delete(id: Long) {
        if (repository.findById(id) == null) {
            throw EventNotFoundException(id)
        }
        repository.deleteById(id)
    }

    override fun getByType(type: String): Flow<Event> {
        val eventType = EventType.valueOf(type.uppercase())
        return repository.findByType(eventType)
    }
}