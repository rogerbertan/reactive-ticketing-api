package dev.rogerbertan.reactive_ticketing_api.event.adapter.`in`.http.dto

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.EventType
import java.time.LocalDate
import java.util.UUID

data class EventResponse(
    val id: UUID?,
    val type: EventType,
    val name: String,
    val date: LocalDate?,
    val description: String
)
