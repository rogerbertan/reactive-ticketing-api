package dev.rogerbertan.reactive_ticketing_api.application.port.`in`.command

import dev.rogerbertan.reactive_ticketing_api.application.domain.EventType
import java.time.LocalDate

data class CreateEventCommand(
    val type: EventType,
    val name: String,
    val date: LocalDate?,
    val description: String
)
