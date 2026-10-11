package dev.rogerbertan.reactive_ticketing_api.adapter.`in`.http

import dev.rogerbertan.reactive_ticketing_api.adapter.`in`.http.dto.EventRequest
import dev.rogerbertan.reactive_ticketing_api.adapter.`in`.http.dto.EventResponse
import dev.rogerbertan.reactive_ticketing_api.application.port.`in`.EventUseCase
import jakarta.validation.Valid
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
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
import kotlin.time.Duration.Companion.seconds

@RestController
@RequestMapping("/v1/events")
class EventController(
    private val service: EventUseCase
) {

    private val eventFlow = MutableSharedFlow<EventResponse>(extraBufferCapacity = 10)

    @GetMapping
    fun getAll(): Flow<EventResponse> {
        return service.getAll().map { it.toResponse() }
    }

    @GetMapping("/categories/{type}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getByType(@PathVariable type: String): Flow<EventResponse> {
        return merge(
            service.getByType(type).map { it.toResponse() },
            eventFlow
        ).onEach { delay(4.seconds) }
    }

    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: Long): EventResponse {
        return service.getById(id).toResponse()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody @Valid request: EventRequest): EventResponse {
        return service.create(request.toCommand()).toResponse()
            .also { eventFlow.emit(it) }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: Long) {
        service.delete(id)
    }
}