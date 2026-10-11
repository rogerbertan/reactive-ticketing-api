package dev.rogerbertan.reactive_ticketing_api.event.adapter.out.persistence

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.EventType
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface EventR2dbcRepository : CoroutineCrudRepository<EventEntity, Long> {
    fun findByType(type: EventType): Flow<EventEntity>
}