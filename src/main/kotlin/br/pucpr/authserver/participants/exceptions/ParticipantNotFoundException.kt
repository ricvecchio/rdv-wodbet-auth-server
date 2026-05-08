package br.pucpr.authserver.participants.exceptions

import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(NOT_FOUND)
class ParticipantNotFoundException(id: Long) : RuntimeException("Participant not found. id=$id")
