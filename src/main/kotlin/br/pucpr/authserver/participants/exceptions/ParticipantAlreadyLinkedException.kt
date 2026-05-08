package br.pucpr.authserver.participants.exceptions

import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(CONFLICT)
class ParticipantAlreadyLinkedException(participantId: Long, eventId: Long) :
    RuntimeException("Participant $participantId is already linked to event $eventId")
