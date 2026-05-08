package br.pucpr.authserver.events.dtos.requests

import br.pucpr.authserver.events.enums.EventStatus
import java.time.LocalDateTime

data class UpdateEventRequest(
    val name: String? = null,
    val description: String? = null,
    val location: String? = null,
    val eventDate: LocalDateTime? = null,
    val status: EventStatus? = null
)
