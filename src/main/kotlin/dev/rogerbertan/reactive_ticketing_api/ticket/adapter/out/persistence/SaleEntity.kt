package dev.rogerbertan.reactive_ticketing_api.ticket.adapter.out.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.UUID

@Table("sales")
data class SaleEntity(
    @Id val id: UUID? = null,
    val ticketId: UUID,
    val quantity: Int,
    val totalPrice: BigDecimal
)
