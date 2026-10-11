package dev.rogerbertan.reactive_ticketing_api.event.adapter.`in`.http.dto

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.EventType
import java.time.LocalDate

data class EventResponse(
    val id: Long?,
    val type: EventType,
    val name: String,
    val date: LocalDate?,
    val description: String
)
