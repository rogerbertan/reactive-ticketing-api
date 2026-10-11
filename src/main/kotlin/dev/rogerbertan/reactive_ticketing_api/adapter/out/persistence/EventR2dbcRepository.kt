package dev.rogerbertan.reactive_ticketing_api.adapter.out.persistence

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface EventR2dbcRepository : CoroutineCrudRepository<EventEntity, Long>