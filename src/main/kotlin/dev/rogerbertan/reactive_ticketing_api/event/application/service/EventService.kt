package dev.rogerbertan.reactive_ticketing_api.event.application.service

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.Event
import dev.rogerbertan.reactive_ticketing_api.event.application.domain.EventType
import dev.rogerbertan.reactive_ticketing_api.event.application.domain.exception.EventNotFoundException
import dev.rogerbertan.reactive_ticketing_api.event.application.domain.exception.InvalidEventTypeException
import dev.rogerbertan.reactive_ticketing_api.event.application.port.`in`.EventUseCase
import dev.rogerbertan.reactive_ticketing_api.event.application.port.`in`.command.CreateEventCommand
import dev.rogerbertan.reactive_ticketing_api.event.application.port.out.EventRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class EventService(
    private val repository: EventRepository
) : EventUseCase {

    @Transactional(readOnly = true)
    override fun getAll(): Flow<Event> {
        return repository.findAll()
    }

    @Transactional(readOnly = true)
    override suspend fun getById(id: UUID): Event {
        return repository.findById(id) ?: throw EventNotFoundException(id)
    }

    @Transactional
    override suspend fun create(cmd: CreateEventCommand): Event {
        return repository.save(Event(
            type = cmd.type,
            name = cmd.name,
            date = cmd.date,
            description = cmd.description
        ))
    }

    @Transactional
    override suspend fun delete(id: UUID) {
        if (repository.findById(id) == null) {
            throw EventNotFoundException(id)
        }
        repository.deleteById(id)
    }

    @Transactional(readOnly = true)
    override fun getByType(type: String): Flow<Event> {
        val eventType = EventType.entries.find { it.name == type.uppercase() }
            ?: throw InvalidEventTypeException(type)
        return repository.findByType(eventType)
    }
}