package dev.rogerbertan.reactive_ticketing_api.ticket.application.service

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Sale
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Ticket
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.TicketType
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception.InsufficientTicketsException
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception.TicketNotFoundException
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.TicketUseCase
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.CreateSaleCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.SellTicketsCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.out.SaleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
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

class SaleServiceTest {

    private val ticketUseCase = mockk<TicketUseCase>()
    private val repository = mockk<SaleRepository>()
    private val service = SaleService(ticketUseCase, repository)

    private val ticketId = UUID.randomUUID()

    private fun soldTicket(price: String, remaining: Int) = Ticket(
        id = ticketId,
        eventId = UUID.randomUUID(),
        type = TicketType.REGULAR,
        price = BigDecimal(price),
        quantity = remaining
    )

    @Test
    fun `total price is the ticket price times the sold quantity`() = runTest {
        coEvery { ticketUseCase.sellTickets(any()) } returns soldTicket("49.90", remaining = 7)
        val saved = slot<Sale>()
        coEvery { repository.save(capture(saved)) } answers { firstArg() }

        service.createSale(CreateSaleCommand(ticketId, 3))

        assertEquals(BigDecimal("149.70"), saved.captured.totalPrice)
    }

    @Test
    fun `sale is registered with the command data and without id`() = runTest {
        coEvery { ticketUseCase.sellTickets(any()) } returns soldTicket("10.00", remaining = 8)
        val saved = slot<Sale>()
        val persisted = Sale(UUID.randomUUID(), ticketId, 2, BigDecimal("20.00"))
        coEvery { repository.save(capture(saved)) } returns persisted

        val result = service.createSale(CreateSaleCommand(ticketId, 2))

        assertNull(saved.captured.id)
        assertEquals(ticketId, saved.captured.ticketId)
        assertEquals(2, saved.captured.quantity)
        assertSame(persisted, result)
    }

    @Test
    fun `stock is decremented before the sale is registered`() = runTest {
        coEvery { ticketUseCase.sellTickets(any()) } returns soldTicket("10.00", remaining = 5)
        coEvery { repository.save(any()) } answers { firstArg() }

        service.createSale(CreateSaleCommand(ticketId, 5))

        coVerifyOrder {
            ticketUseCase.sellTickets(SellTicketsCommand(ticketId, 5))
            repository.save(any())
        }
    }

    @Test
    fun `sale is not registered when there are not enough tickets`() = runTest {
        coEvery { ticketUseCase.sellTickets(any()) } throws InsufficientTicketsException()

        assertThrows<InsufficientTicketsException> {
            service.createSale(CreateSaleCommand(ticketId, 50))
        }

        coVerify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `sale is not registered when the ticket does not exist`() = runTest {
        coEvery { ticketUseCase.sellTickets(any()) } throws TicketNotFoundException(ticketId)

        assertThrows<TicketNotFoundException> {
            service.createSale(CreateSaleCommand(ticketId, 1))
        }

        coVerify(exactly = 0) { repository.save(any()) }
    }
}