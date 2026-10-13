package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http

import dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto.SaleRequest
import dev.rogerbertan.reactive_ticketing_api.ticket.adapter.`in`.http.dto.TicketResponse
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Sale
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.TicketType
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.SaleUseCase
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.TicketUseCase
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.AddTicketsCommand
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals

class TicketControllerTest {

    private val ticketService = mockk<TicketUseCase>()
    private val saleService = mockk<SaleUseCase>()
    private val controller = TicketController(ticketService, saleService)

    private val eventId = UUID.randomUUID()

    private fun ticket(eventId: UUID, quantity: Int) = Ticket(
        id = UUID.randomUUID(),
        eventId = eventId,
        type = TicketType.REGULAR,
        price = BigDecimal("30.00"),
        quantity = quantity
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.subscribeToEventStream(): List<TicketResponse> {
        every { ticketService.getTicketsByEventId(eventId) } returns emptyFlow()
        val received = mutableListOf<TicketResponse>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.getTicketsByEventId(eventId).toList(received)
        }
        return received
    }

    @Test
    fun `event stream only receives live updates of tickets from the same event`() = runTest {
        val received = subscribeToEventStream()
        val otherEventTicket = ticket(UUID.randomUUID(), quantity = 10)
        val sameEventTicket = ticket(eventId, quantity = 20)
        coEvery { ticketService.addTicket(AddTicketsCommand(otherEventTicket.id!!, 1)) } returns otherEventTicket
        coEvery { ticketService.addTicket(AddTicketsCommand(sameEventTicket.id!!, 1)) } returns sameEventTicket

        controller.addTicket(otherEventTicket.id!!, 1)
        controller.addTicket(sameEventTicket.id!!, 1)

        assertEquals(listOf(sameEventTicket.toResponse()), received)
    }

    @Test
    fun `a sale pushes the ticket with the stock after the sale to the event stream`() = runTest {
        val received = subscribeToEventStream()
        val afterSale = ticket(eventId, quantity = 3)
        val ticketId = afterSale.id!!
        coEvery { saleService.createSale(any()) } returns
            Sale(UUID.randomUUID(), ticketId, 2, BigDecimal("60.00"))
        coEvery { ticketService.getTicketsById(ticketId) } returns afterSale

        controller.createSale(SaleRequest(ticketId, 2))

        assertEquals(listOf(afterSale.toResponse()), received)
    }
}