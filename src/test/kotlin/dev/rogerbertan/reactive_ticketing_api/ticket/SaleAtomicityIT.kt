package dev.rogerbertan.reactive_ticketing_api.ticket

import dev.rogerbertan.reactive_ticketing_api.support.IntegrationTest
import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Sale
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.SaleUseCase
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.CreateSaleCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.out.SaleRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import kotlin.test.assertEquals

@Import(SaleAtomicityIT.FailingSaleRepositoryConfig::class)
class SaleAtomicityIT : IntegrationTest() {

    @TestConfiguration
    class FailingSaleRepositoryConfig {
        @Bean
        @Primary
        fun failingSaleRepository(): SaleRepository = object : SaleRepository {
            override suspend fun save(sale: Sale): Sale = throw IllegalStateException("sale storage is down")
        }
    }

    @Autowired
    private lateinit var saleUseCase: SaleUseCase

    @Test
    fun `stock decrement is rolled back when the sale cannot be registered`() = runBlocking {
        val ticket = createTicket(createEvent().id!!, price = "10.00", quantity = 5)

        assertThrows<IllegalStateException> {
            runBlocking { saleUseCase.createSale(CreateSaleCommand(ticket.id!!, 2)) }
        }

        assertEquals(5, stockOf(ticket.id!!))
    }
}