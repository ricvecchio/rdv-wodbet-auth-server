package br.pucpr.authserver.roles.controllers

import br.pucpr.authserver.roles.dtos.requests.CreateRoleRequest
import br.pucpr.authserver.roles.dtos.responses.RoleResponse
import br.pucpr.authserver.roles.services.RoleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/roles")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "jwt-auth")
@Tag(name = "Roles", description = "Gerenciamento de perfis de acesso")
class RoleController(val service: RoleService) {

    @PostMapping
    @Operation(summary = "Criar role", description = "Cria uma nova role. Requer perfil ADMIN.")
    fun insert(
        @RequestBody @Valid role: CreateRoleRequest
    ) = service.insert(role.toRole())
        .let { RoleResponse(it) }
        .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping
    @Operation(summary = "Listar roles", description = "Lista todas as roles disponíveis. Requer perfil ADMIN.")
    fun list() = service.findAll()
        .map { RoleResponse(it) }
        .let { ResponseEntity.ok(it) }
}
