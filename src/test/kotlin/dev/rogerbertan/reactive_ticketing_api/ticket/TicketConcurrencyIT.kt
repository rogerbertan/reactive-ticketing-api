package dev.rogerbertan.reactive_ticketing_api.ticket

import dev.rogerbertan.reactive_ticketing_api.support.IntegrationTest
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.exception.InsufficientTicketsException
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.SaleUseCase
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.CreateSaleCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TicketConcurrencyIT : IntegrationTest() {

    @Autowired
    private lateinit var saleUseCase: SaleUseCase

    @Test
    fun `concurrent sales never sell more tickets than the stock`() = runBlocking {
        val ticket = createTicket(createEvent().id!!, price = "10.00", quantity = 5)

        val results = (1..10).map {
            async(Dispatchers.IO) {
                runCatching { saleUseCase.createSale(CreateSaleCommand(ticket.id!!, 1)) }
            }
        }.awaitAll()

        val (succeeded, failed) = results.partition { it.isSuccess }
        assertEquals(5, succeeded.size)
        assertTrue(failed.all { it.exceptionOrNull() is InsufficientTicketsException })
        assertEquals(0, stockOf(ticket.id!!))
        assertEquals(5, salesTotalsOf(ticket.id).size)
    }
}