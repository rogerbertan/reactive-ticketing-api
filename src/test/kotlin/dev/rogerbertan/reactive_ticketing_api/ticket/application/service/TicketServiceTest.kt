package dev.rogerbertan.reactive_ticketing_api.ticket.application.service

import dev.rogerbertan.reactive_ticketing_api.event.application.domain.exception.EventNotFoundException
import dev.rogerbertan.reactive_ticketing_api.event.application.port.`in`.EventUseCase
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.TicketType
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception.InsufficientTicketsException
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception.TicketNotFoundException
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.AddTicketsCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.CreateTicketCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.SellTicketsCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.out.TicketRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class TicketServiceTest {

    private val repository = mockk<TicketRepository>()
    private val eventUseCase = mockk<EventUseCase>()
    private val service = TicketService(repository, eventUseCase)

    private val ticketId = UUID.randomUUID()

    private fun ticketWithStock(quantity: Int) = Ticket(
        id = ticketId,
        eventId = UUID.randomUUID(),
        type = TicketType.VIP,
        price = BigDecimal("120.00"),
        quantity = quantity
    )

    private fun saveReturnsArgument() {
        coEvery { repository.save(any()) } answers { firstArg() }
    }

    @Test
    fun `selling locks the ticket row instead of using a plain read`() = runTest {
        coEvery { repository.getTicketByIdForUpdate(ticketId) } returns ticketWithStock(10)
        saveReturnsArgument()

        service.sellTickets(SellTicketsCommand(ticketId, 2))

        coVerify(exactly = 1) { repository.getTicketByIdForUpdate(ticketId) }
        coVerify(exactly = 0) { repository.getTicketById(any()) }
    }

    @Test
    fun `adding tickets locks the ticket row instead of using a plain read`() = runTest {
        coEvery { repository.getTicketByIdForUpdate(ticketId) } returns ticketWithStock(10)
        saveReturnsArgument()

        service.addTicket(AddTicketsCommand(ticketId, 5))

        coVerify(exactly = 1) { repository.getTicketByIdForUpdate(ticketId) }
        coVerify(exactly = 0) { repository.getTicketById(any()) }
    }

    @Test
    fun `selling persists the ticket with the decremented stock`() = runTest {
        coEvery { repository.getTicketByIdForUpdate(ticketId) } returns ticketWithStock(10)
        val saved = slot<Ticket>()
        coEvery { repository.save(capture(saved)) } answers { firstArg() }

        val result = service.sellTickets(SellTicketsCommand(ticketId, 4))

        assertEquals(6, saved.captured.quantity)
        assertSame(saved.captured, result)
    }

    @Test
    fun `adding tickets persists the ticket with the incremented stock`() = runTest {
        coEvery { repository.getTicketByIdForUpdate(ticketId) } returns ticketWithStock(10)
        val saved = slot<Ticket>()
        coEvery { repository.save(capture(saved)) } answers { firstArg() }

        val result = service.addTicket(AddTicketsCommand(ticketId, 5))

        assertEquals(15, saved.captured.quantity)
        assertSame(saved.captured, result)
    }

    @Test
    fun `selling more than the stock does not persist anything`() = runTest {
        coEvery { repository.getTicketByIdForUpdate(ticketId) } returns ticketWithStock(3)

        assertThrows<InsufficientTicketsException> {
            service.sellTickets(SellTicketsCommand(ticketId, 4))
        }

        coVerify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `selling a missing ticket fails with not found and does not persist anything`() = runTest {
        coEvery { repository.getTicketByIdForUpdate(ticketId) } returns null

        assertThrows<TicketNotFoundException> {
            service.sellTickets(SellTicketsCommand(ticketId, 1))
        }

        coVerify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `adding to a missing ticket fails with not found and does not persist anything`() = runTest {
        coEvery { repository.getTicketByIdForUpdate(ticketId) } returns null

        assertThrows<TicketNotFoundException> {
            service.addTicket(AddTicketsCommand(ticketId, 1))
        }

        coVerify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `getting a missing ticket by id fails with not found`() = runTest {
        coEvery { repository.getTicketById(ticketId) } returns null

        assertThrows<TicketNotFoundException> { service.getTicketsById(ticketId) }
    }

    @Test
    fun `creating a ticket persists a new ticket with the command data`() = runTest {
        val eventId = UUID.randomUUID()
        coEvery { eventUseCase.getById(eventId) } returns mockk()
        val saved = slot<Ticket>()
        coEvery { repository.save(capture(saved)) } answers { firstArg<Ticket>().copy(id = ticketId) }

        val result = service.createTicket(
            CreateTicketCommand(eventId, TicketType.STUDENT, BigDecimal("25.00"), 100)
        )

        assertNull(saved.captured.id)
        assertEquals(eventId, saved.captured.eventId)
        assertEquals(TicketType.STUDENT, saved.captured.type)
        assertEquals(BigDecimal("25.00"), saved.captured.price)
        assertEquals(100, saved.captured.quantity)
        assertEquals(ticketId, result.id)
    }

    @Test
    fun `creating a ticket for a missing event fails with not found and does not persist anything`() = runTest {
        val eventId = UUID.randomUUID()
        coEvery { eventUseCase.getById(eventId) } throws EventNotFoundException(eventId)

        assertThrows<EventNotFoundException> {
            service.createTicket(CreateTicketCommand(eventId, TicketType.VIP, BigDecimal("10.00"), 1))
        }

        coVerify(exactly = 0) { repository.save(any()) }
    }
}