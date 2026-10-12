package dev.rogerbertan.reactive_ticketing_api.event.adapter.out.persistence

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.EventType
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface EventR2dbcRepository : CoroutineCrudRepository<EventEntity, UUID> {
    fun findByType(type: EventType): Flow<EventEntity>
}