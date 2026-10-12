package dev.rogerbertan.reactive_ticketing_api.event.application.domain.exception

import java.util.UUID

class EventNotFoundException(id: UUID) : RuntimeException("Event not found: $id")