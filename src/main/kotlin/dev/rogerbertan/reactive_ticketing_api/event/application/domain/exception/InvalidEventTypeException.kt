package dev.rogerbertan.reactive_ticketing_api.event.application.domain.exception

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.EventType

class InvalidEventTypeException(type: String) :
    RuntimeException("Invalid event type: $type. Valid types: ${EventType.entries.joinToString()}")