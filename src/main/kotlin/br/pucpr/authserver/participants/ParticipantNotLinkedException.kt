package br.pucpr.authserver.participants

import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(BAD_REQUEST)
class ParticipantNotLinkedException(participantId: Long, eventId: Long) :
    RuntimeException("Participant $participantId is not linked to event $eventId")

