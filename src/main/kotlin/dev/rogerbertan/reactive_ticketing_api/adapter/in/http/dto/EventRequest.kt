package dev.rogerbertan.reactive_ticketing_api.adapter.`in`.http.dto

import dev.rogerbertan.reactive_ticketing_api.application.domain.EventType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class EventRequest(
    @field:NotNull
    val type: EventType,
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,
    val date: LocalDate?,
    @field:NotBlank
    @field:Size(max = 200)
    val description: String
)
