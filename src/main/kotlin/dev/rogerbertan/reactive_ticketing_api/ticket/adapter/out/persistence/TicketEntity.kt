package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.out.persistence

import dev.rogerbertan.reactive_ticketing_api.ticket.application.domain.TicketType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.UUID

@Table("tickets")
data class TicketEntity(
    @Id val id: UUID?,
    val eventId: UUID,
    val type: TicketType,
    val price: BigDecimal,
    var quantity: Int
)
