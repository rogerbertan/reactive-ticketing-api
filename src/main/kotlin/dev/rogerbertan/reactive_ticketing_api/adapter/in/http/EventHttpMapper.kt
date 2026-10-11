package dev.rogerbertan.reactive_ticketing_api.adapter.`in`.http

import dev.rogerbertan.reactive_ticketing_api.adapter.`in`.http.dto.EventRequest
import dev.rogerbertan.reactive_ticketing_api.adapter.`in`.http.dto.EventResponse
import dev.rogerbertan.reactive_ticketing_api.application.domain.Event
import dev.rogerbertan.reactive_ticketing_api.application.port.`in`.command.CreateEventCommand

fun Event.toResponse(): EventResponse {
    return EventResponse(
        id = id,
        type = type,
        name = name,
        date = date,
        description = description
    )
}

fun EventRequest.toCommand(): CreateEventCommand {
    return CreateEventCommand(
        type = type,
        name = name,
        date = date,
        description = description
    )
}