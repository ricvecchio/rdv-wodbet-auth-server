package br.pucpr.authserver.participants.responses

import br.pucpr.authserver.participants.Participant
import java.time.LocalDateTime

data class ParticipantResponse(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val eventId: Long?,
    val eventName: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    constructor(participant: Participant) : this(
        id = participant.id!!,
        name = participant.name,
        email = participant.email,
        phone = participant.phone,
        eventId = participant.event?.id,
        eventName = participant.event?.name,
        createdAt = participant.createdAt,
        updatedAt = participant.updatedAt
    )
}

