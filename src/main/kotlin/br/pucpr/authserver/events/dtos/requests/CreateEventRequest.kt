package br.pucpr.authserver.events.dtos.requests

import br.pucpr.authserver.events.entities.Event
import br.pucpr.authserver.events.enums.EventStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class CreateEventRequest(
    @field:NotBlank
    val name: String?,
    val description: String? = null,
    val location: String? = null,
    @field:NotNull
    val eventDate: LocalDateTime?,
    val status: EventStatus? = EventStatus.SCHEDULED
) {
    fun toEvent() = Event(
        name = name!!,
        description = description ?: "",
        location = location ?: "",
        eventDate = eventDate!!,
        status = status ?: EventStatus.SCHEDULED
    )
}
