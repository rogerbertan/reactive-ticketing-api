package dev.rogerbertan.reactive_ticketing_api.adapter.`in`.http

import dev.rogerbertan.reactive_ticketing_api.adapter.`in`.http.dto.EventRequest
import dev.rogerbertan.reactive_ticketing_api.adapter.`in`.http.dto.EventResponse
import dev.rogerbertan.reactive_ticketing_api.application.port.`in`.EventUseCase
import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
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

    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: Long): EventResponse {
        return service.getById(id).toResponse()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody @Valid request: EventRequest): EventResponse {
        return service.create(request.toCommand()).toResponse()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: Long) {
        service.delete(id)
    }
}