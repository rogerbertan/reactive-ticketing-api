package dev.rogerbertan.reactive_ticketing_api.event.application.domain.exception

class EventNotFoundException(id: Long) : RuntimeException("Event not found: $id")