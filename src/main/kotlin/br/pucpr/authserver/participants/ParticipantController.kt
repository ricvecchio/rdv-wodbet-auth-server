package br.pucpr.authserver.participants

import br.pucpr.authserver.participants.requests.CreateParticipantRequest
import br.pucpr.authserver.participants.requests.UpdateParticipantRequest
import br.pucpr.authserver.participants.responses.ParticipantResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/participants")
class ParticipantController(private val service: ParticipantService) {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "jwt-auth")
    fun create(
        @Valid @RequestBody request: CreateParticipantRequest
    ): ResponseEntity<ParticipantResponse> =
        service.create(request.toParticipant())
            .let { ParticipantResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ParticipantResponse> =
        service.findById(id)
            .let { ParticipantResponse(it) }
            .let { ResponseEntity.ok(it) }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "jwt-auth")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateParticipantRequest
    ): ResponseEntity<ParticipantResponse> =
        service.update(id, request)
            .let { ParticipantResponse(it) }
            .let { ResponseEntity.ok(it) }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "jwt-auth")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}

