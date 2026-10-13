package dev.rogerbertan.reactive_ticketing_api.event.application.service

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.Event
import dev.rogerbertan.reactive_ticketing_api.event.application.domain.EventType
import dev.rogerbertan.reactive_ticketing_api.event.application.domain.exception.EventNotFoundException
import dev.rogerbertan.reactive_ticketing_api.event.application.domain.exception.InvalidEventTypeException
import dev.rogerbertan.reactive_ticketing_api.event.application.port.`in`.command.CreateEventCommand
import dev.rogerbertan.reactive_ticketing_api.event.application.port.out.EventRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EventServiceTest {

    private val repository = mockk<EventRepository>()
    private val service = EventService(repository)

    private val eventId = UUID.randomUUID()
    private val event = Event(
        id = eventId,
        type = EventType.SHOW,
        name = "Show",
        date = LocalDate.of(2026, 12, 1),
        description = "Description"
    )

    @Test
    fun `getting a missing event by id fails with not found`() = runTest {
        coEvery { repository.findById(eventId) } returns null

        assertThrows<EventNotFoundException> { service.getById(eventId) }
    }

    @Test
    fun `deleting a missing event fails with not found and deletes nothing`() = runTest {
        coEvery { repository.findById(eventId) } returns null

        assertThrows<EventNotFoundException> { service.delete(eventId) }

        coVerify(exactly = 0) { repository.deleteById(any()) }
    }

    @Test
    fun `deleting an existing event deletes it by id`() = runTest {
        coEvery { repository.findById(eventId) } returns event
        coEvery { repository.deleteById(eventId) } just runs

        service.delete(eventId)

        coVerify(exactly = 1) { repository.deleteById(eventId) }
    }

    @Test
    fun `category lookup ignores the case of the given type`() = runTest {
        every { repository.findByType(EventType.SHOW) } returns flowOf(event)

        val result = service.getByType("sHoW").toList()

        assertEquals(listOf(event), result)
        verify(exactly = 1) { repository.findByType(EventType.SHOW) }
    }

    @Test
    fun `category lookup with an unknown type is rejected without querying`() {
        assertThrows<InvalidEventTypeException> { service.getByType("cinema") }

        verify(exactly = 0) { repository.findByType(any()) }
    }

    @Test
    fun `creating an event persists a new event with the command data`() = runTest {
        val saved = slot<Event>()
        coEvery { repository.save(capture(saved)) } answers { firstArg<Event>().copy(id = eventId) }

        val result = service.create(
            CreateEventCommand(EventType.WORKSHOP, "Kotlin", LocalDate.of(2026, 11, 20), "Hands-on")
        )

        assertNull(saved.captured.id)
        assertEquals(EventType.WORKSHOP, saved.captured.type)
        assertEquals("Kotlin", saved.captured.name)
        assertEquals(LocalDate.of(2026, 11, 20), saved.captured.date)
        assertEquals("Hands-on", saved.captured.description)
        assertEquals(eventId, result.id)
    }
}