package dev.rogerbertan.reactive_ticketing_api.adapter.`in`.http

import dev.rogerbertan.reactive_ticketing_api.application.domain.Event
import dev.rogerbertan.reactive_ticketing_api.application.port.`in`.EventUseCase
import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/events")
class EventController(
    private val service: EventUseCase
) {

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getAll(): Flow<Event> {
        return service.getAll()
    }
}