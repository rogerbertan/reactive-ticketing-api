package dev.rogerbertan.reactive_ticketing_api.adapter.`in`.http

import dev.rogerbertan.reactive_ticketing_api.adapter.`in`.http.dto.EventResponse
import dev.rogerbertan.reactive_ticketing_api.application.port.`in`.EventUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/events")
class EventController(
    private val service: EventUseCase
) {

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getAll(): Flow<EventResponse> {
        return service.getAll().map { it.toResponse() }
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getById(@PathVariable id: Long): EventResponse {
        return service.getById(id).toResponse()
    }
}