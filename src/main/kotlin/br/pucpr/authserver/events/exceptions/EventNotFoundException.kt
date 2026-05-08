package br.pucpr.authserver.events.exceptions

import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(NOT_FOUND)
class EventNotFoundException(id: Long) : RuntimeException("Event not found. id=$id")
