package dev.rogerbertan.reactive_ticketing_api.event

import dev.rogerbertan.reactive_ticketing_api.event.adapter.`in`.http.dto.EventResponse
import dev.rogerbertan.reactive_ticketing_api.event.application.domain.EventType
import dev.rogerbertan.reactive_ticketing_api.support.IntegrationTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.test.web.reactive.server.expectBody
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EventIT : IntegrationTest() {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["2026-12-01"])
    fun `a created event is read back with the same type and date`(date: String?) {
        val created = webTestClient.post().uri("/v1/events")
            .bodyValue(
                mapOf(
                    "type" to "WORKSHOP",
                    "name" to "Kotlin",
                    "date" to date,
                    "description" to "Hands-on"
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody<EventResponse>()
            .returnResult().responseBody!!

        val read = webTestClient.get().uri("/v1/events/{id}", created.id)
            .exchange()
            .expectStatus().isOk
            .expectBody<EventResponse>()
            .returnResult().responseBody!!

        assertEquals(EventType.WORKSHOP, read.type)
        assertEquals(date?.let(LocalDate::parse), read.date)
    }

    @Test
    fun `listing an unknown category returns bad request`() {
        webTestClient.get().uri("/v1/events/categories/{type}", "cinema")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody().jsonPath("$.title").isEqualTo("Invalid Event Type")
    }

    @Test
    fun `deleting an event also removes its tickets and their sales`() = runBlocking {
        val eventId = createEvent().id!!
        val ticket = createTicket(eventId, price = "10.00", quantity = 5)
        webTestClient.post().uri("/v1/tickets/sale")
            .bodyValue(mapOf("ticketId" to ticket.id, "quantity" to 1))
            .exchange()
            .expectStatus().isCreated

        webTestClient.delete().uri("/v1/events/{id}", eventId)
            .exchange()
            .expectStatus().isNoContent

        assertNull(eventRepository.findById(eventId))
        assertNull(ticketRepository.findById(ticket.id!!))
        assertTrue(salesTotalsOf(ticket.id).isEmpty())
    }
}