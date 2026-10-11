package dev.rogerbertan.reactive_ticketing_api.event.adapter.out.persistence

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.Event

fun EventEntity.toDomain(): Event {
    return Event(
        id = id,
        type = type,
        name = name,
        date = date,
        description = description
    )
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        type = type,
        name = name,
        date = date,
        description = description
    )
}