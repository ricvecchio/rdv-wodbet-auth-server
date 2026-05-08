package br.pucpr.authserver.participants.controllers

import br.pucpr.authserver.participants.dtos.requests.CreateParticipantRequest
import br.pucpr.authserver.participants.dtos.requests.UpdateParticipantRequest
import br.pucpr.authserver.participants.dtos.responses.ParticipantResponse
import br.pucpr.authserver.participants.services.ParticipantService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/participants")
@Tag(name = "Participants", description = "Gerenciamento de participantes")
class ParticipantController(private val service: ParticipantService) {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "jwt-auth")
    @Operation(summary = "Criar participante", description = "Cria um novo participante. Requer autenticação.")
    fun create(@Valid @RequestBody request: CreateParticipantRequest): ResponseEntity<ParticipantResponse> =
        service.create(request.toParticipant()).let { ParticipantResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping
    @Operation(summary = "Listar participantes", description = "Lista todos os participantes ordenados por nome (ASC ou DESC).")
    fun list(@RequestParam(defaultValue = "ASC") sortDir: String): ResponseEntity<List<ParticipantResponse>> =
        service.findAll(sortDir).map { ParticipantResponse(it) }.let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar participante por ID")
    fun getById(@PathVariable id: Long): ResponseEntity<ParticipantResponse> =
        service.findById(id).let { ParticipantResponse(it) }.let { ResponseEntity.ok(it) }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "jwt-auth")
    @Operation(summary = "Atualizar participante", description = "Atualiza parcialmente os dados de um participante. Requer autenticação.")
    fun update(@PathVariable id: Long, @RequestBody request: UpdateParticipantRequest): ResponseEntity<ParticipantResponse> =
        service.update(id, request).let { ParticipantResponse(it) }.let { ResponseEntity.ok(it) }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "jwt-auth")
    @Operation(summary = "Remover participante", description = "Remove um participante pelo ID. Requer perfil ADMIN.")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
