package dev.rogerbertan.reactive_ticketing_api.event.application.domain

import java.time.LocalDate

data class Event(
    val id: Long? = null,
    val type: EventType,
    val name: String,
    val date: LocalDate?,
    val description: String,
)