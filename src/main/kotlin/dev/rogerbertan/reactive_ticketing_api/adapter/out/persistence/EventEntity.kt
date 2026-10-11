package dev.rogerbertan.reactive_ticketing_api.adapter.out.persistence

import dev.rogerbertan.reactive_ticketing_api.application.domain.EventType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

@Table("events")
data class EventEntity(
    @Id val id: Long? = null,
    val type: EventType,
    val name: String,
    val date: LocalDate?,
    val description: String,
)
