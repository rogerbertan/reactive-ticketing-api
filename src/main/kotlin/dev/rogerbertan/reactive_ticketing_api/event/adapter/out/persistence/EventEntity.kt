package dev.rogerbertan.reactive_ticketing_api.event.adapter.out.persistence

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.EventType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.util.UUID

@Table("events")
data class EventEntity(
    @Id val id: UUID? = null,
    val type: EventType,
    val name: String,
    val date: LocalDate?,
    val description: String,
)
