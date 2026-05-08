package br.pucpr.authserver.events.dtos.responses

import br.pucpr.authserver.events.entities.Event
import br.pucpr.authserver.events.enums.EventStatus
import br.pucpr.authserver.participants.dtos.responses.ParticipantResponse
import java.time.LocalDateTime

data class EventResponse(
    val id: Long,
    val name: String,
    val description: String,
    val location: String,
    val eventDate: LocalDateTime,
    val status: EventStatus,
    val participants: List<ParticipantResponse>,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    constructor(event: Event) : this(
        id = event.id!!,
        name = event.name,
        description = event.description,
        location = event.location,
        eventDate = event.eventDate,
        status = event.status,
        participants = event.participants.map { ParticipantResponse(it) },
        createdAt = event.createdAt,
        updatedAt = event.updatedAt
    )
}
