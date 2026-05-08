package br.pucpr.authserver.events

import br.pucpr.authserver.events.requests.CreateEventRequest
import br.pucpr.authserver.events.requests.UpdateEventRequest
import br.pucpr.authserver.events.responses.EventResponse
import br.pucpr.authserver.participants.responses.ParticipantResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/events")
class EventController(private val service: EventService) {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "jwt-auth")
    fun create(
        @Valid @RequestBody request: CreateEventRequest
    ): ResponseEntity<EventResponse> =
        service.create(request.toEvent())
            .let { EventResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping
    fun list(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) status: EventStatus?,
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime?,
        @RequestParam(defaultValue = "eventDate") sortBy: String,
        @RequestParam(defaultValue = "ASC") direction: String
    ): ResponseEntity<List<EventResponse>> =
        service.findAll(name, status, location, startDate, endDate, sortBy, direction)
            .map { EventResponse(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<EventResponse> =
        service.findById(id)
            .let { EventResponse(it) }
            .let { ResponseEntity.ok(it) }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "jwt-auth")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateEventRequest
    ): ResponseEntity<EventResponse> =
        service.update(id, request)
            .let { EventResponse(it) }
            .let { ResponseEntity.ok(it) }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "jwt-auth")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{eventId}/participants/{participantId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "jwt-auth")
    fun addParticipant(
        @PathVariable eventId: Long,
        @PathVariable participantId: Long
    ): ResponseEntity<EventResponse> =
        service.addParticipant(eventId, participantId)
            .let { EventResponse(it) }
            .let { ResponseEntity.ok(it) }

    @DeleteMapping("/{eventId}/participants/{participantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "jwt-auth")
    fun removeParticipant(
        @PathVariable eventId: Long,
        @PathVariable participantId: Long
    ): ResponseEntity<Void> {
        service.removeParticipant(eventId, participantId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{eventId}/participants")
    fun listParticipants(
        @PathVariable eventId: Long
    ): ResponseEntity<List<ParticipantResponse>> =
        service.listParticipants(eventId)
            .map { ParticipantResponse(it) }
            .let { ResponseEntity.ok(it) }
}

