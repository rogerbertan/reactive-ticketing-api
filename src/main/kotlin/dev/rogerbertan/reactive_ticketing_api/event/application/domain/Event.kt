package dev.rogerbertan.reactive_ticketing_api.event.application.domain

import java.time.LocalDate
import java.util.UUID

data class Event(
    val id: UUID? = null,
    val type: EventType,
    val name: String,
    val date: LocalDate?,
    val description: String,
)