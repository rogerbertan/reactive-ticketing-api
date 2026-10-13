package dev.rogerbertan.reactive_ticketing_api.ticket

import dev.rogerbertan.reactive_ticketing_api.support.IntegrationTest
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.out.TicketRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.assertEquals

class TicketPersistenceIT : IntegrationTest() {

    @Autowired
    private lateinit var ticketPort: TicketRepository

    @Test
    fun `the database rejects a negative stock even when the domain is bypassed`() = runBlocking {
        val ticket = createTicket(createEvent().id!!, price = "10.00", quantity = 1)

        assertThrows<DataIntegrityViolationException> {
            runBlocking { ticketRepository.save(ticket.copy(quantity = -1)) }
        }

        assertEquals(1, stockOf(ticket.id!!))
    }

    @Test
    fun `tickets by event only returns the tickets of that event`() = runBlocking {
        val eventId = createEvent().id!!
        val vip = createTicket(eventId, price = "100.00", quantity = 1)
        val regular = createTicket(eventId, price = "50.00", quantity = 1)
        createTicket(createEvent().id!!, price = "10.00", quantity = 1)

        val ids = ticketPort.getTicketsByEventId(eventId).toList().map { it.id }

        assertEquals(setOf(vip.id, regular.id), ids.toSet())
        assertEquals(2, ids.size)
    }
}