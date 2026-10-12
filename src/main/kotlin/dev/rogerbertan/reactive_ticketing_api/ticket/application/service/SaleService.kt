package dev.rogerbertan.reactive_ticketing_api.ticket.application.service

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.Sale
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.SaleUseCase
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.TicketUseCase
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.CreateSaleCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.`in`.command.SellTicketsCommand
import dev.rogerbertan.reactive_ticketing_api.ticket.application.port.out.SaleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SaleService(
    private val ticketUseCase: TicketUseCase,
    private val repository: SaleRepository
) : SaleUseCase {

    @Transactional
    override suspend fun createSale(cmd: CreateSaleCommand): Sale {
        val ticket = ticketUseCase.sellTickets(
            SellTicketsCommand(
                ticketId = cmd.ticketId,
                quantity = cmd.quantity
            )
        )
        val totalPrice = ticket.price * cmd.quantity.toBigDecimal()
        val sale = Sale(
            id = null,
            ticketId = cmd.ticketId,
            quantity = cmd.quantity,
            totalPrice = totalPrice
        )
        return repository.save(sale)
    }
}

