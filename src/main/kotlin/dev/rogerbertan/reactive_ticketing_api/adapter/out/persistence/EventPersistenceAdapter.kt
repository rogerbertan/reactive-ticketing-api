package dev.rogerbertan.reactive_ticketing_api.adapter.out.persistence

import dev.rogerbertan.reactive_ticketing_api.application.domain.Event
import dev.rogerbertan.reactive_ticketing_api.application.port.out.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component

@Component
class EventPersistenceAdapter(
    private val repository: EventR2dbcRepository
) : EventRepository {

    override fun findAll(): Flow<Event> {
        return repository.findAll().map { it.toDomain() }
    }

    override suspend fun findById(id: Long): Event? {
        return repository.findById(id)?.toDomain()
    }

    override suspend fun save(event: Event): Event {
        return repository.save(event.toEntity()).toDomain()
    }

    override suspend fun deleteById(id: Long) {
        repository.deleteById(id)
    }
}