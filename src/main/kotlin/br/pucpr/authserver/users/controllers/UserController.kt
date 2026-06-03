package br.pucpr.authserver.users.controllers

import br.pucpr.authserver.exceptions.ForbiddenException
import br.pucpr.authserver.security.UserToken
import br.pucpr.authserver.users.dtos.requests.CreateUserRequest
import br.pucpr.authserver.users.dtos.requests.PhoneConfirmRequest
import br.pucpr.authserver.users.dtos.requests.PhoneLoginRequest
import br.pucpr.authserver.users.dtos.requests.UpdateUserProfileRequest
import br.pucpr.authserver.users.dtos.requests.UpdateUserRequest
import br.pucpr.authserver.users.dtos.responses.BackendUserResponse
import br.pucpr.authserver.users.dtos.responses.UserResponse
import br.pucpr.authserver.users.enums.SortDir
import br.pucpr.authserver.users.services.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Gerenciamento de usuários")
class UserController(val service: UserService) {

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Lista todos os usuários. Filtra por role se informado.")
    fun list(
        @RequestParam sortDir: String? = null,
        @RequestParam role: String? = null
    ): ResponseEntity<List<UserResponse>> {
        val users = if (role != null) service.findByRole(role)
        else service.findAll(SortDir.find(sortDir ?: "ASC"))
        return users.map { UserResponse(it) }.let { ResponseEntity.ok(it) }
    }

    @PostMapping
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário com a role USER atribuída automaticamente.")
    fun insert(
        @Valid @RequestBody user: CreateUserRequest
    ) = service.insert(user.toUser())
        .let { UserResponse(it) }
        .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    /**
     * Phone + UUID login (iOS flow).
     * HTTP 200 → user already exists and is active (returns BackendUserResponse)
     * HTTP 202 → confirmation code sent (no body)
     */
    @PostMapping("/login")
    @Operation(summary = "Login por telefone", description = "Autentica por telefone + uuid. Retorna usuário (200) ou envia código (202).")
    fun phoneLogin(
        @Valid @RequestBody request: PhoneLoginRequest
    ): ResponseEntity<*> {
        val user = service.phoneLogin(request.phone!!, request.uuid!!)
        return if (user != null) {
            ResponseEntity.ok(BackendUserResponse(user))
        } else {
            ResponseEntity.status(HttpStatus.ACCEPTED).build<Void>()
        }
    }

    /**
     * Confirm phone code (iOS flow).
     * HTTP 200 → code valid, returns BackendUserResponse
     */
    @PostMapping("/confirm")
    @Operation(summary = "Confirmar código", description = "Valida o código de confirmação e retorna o usuário.")
    fun confirmPhone(
        @Valid @RequestBody request: PhoneConfirmRequest
    ): ResponseEntity<BackendUserResponse> {
        val user = service.confirmPhone(request.phone!!, request.uuid!!, request.code!!)
        return ResponseEntity.ok(BackendUserResponse(user))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    fun getById(
        @PathVariable id: Long
    ) = service.findById(id)
        .let { UserResponse(it) }
        .let { ResponseEntity.ok(it) }

    /**
     * Update user profile (iOS flow) – PUT /users/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar perfil do usuário (iOS)", description = "Atualiza nome e descrição do usuário.")
    fun updateProfile(
        @PathVariable id: Long,
        @RequestBody request: UpdateUserProfileRequest
    ): ResponseEntity<BackendUserResponse> {
        val user = service.updateProfile(id, request.name, request.description)
        return ResponseEntity.ok(BackendUserResponse(user))
    }

    @PreAuthorize("permitAll()")
    @SecurityRequirement(name = "jwt-auth")
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar usuário (legado)", description = "Atualiza o nome do usuário. Somente o próprio usuário ou ADMIN pode alterar.")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody user: UpdateUserRequest,
        auth: Authentication
    ): ResponseEntity<UserResponse> {
        val token = auth.principal as? UserToken ?: throw ForbiddenException()
        if (token.id != id && !token.isAdmin) throw ForbiddenException("Update is not allowed")
        return service.update(id, user.name!!)
            ?.let { UserResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.noContent().build()
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "jwt-auth")
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover usuário", description = "Remove um usuário pelo ID. Requer perfil ADMIN.")
    fun delete(
        @PathVariable id: Long
    ) = service.delete(id)

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "jwt-auth")
    @PutMapping("/{id}/roles/{role}")
    @Operation(summary = "Adicionar role ao usuário", description = "Atribui uma role a um usuário. Requer perfil ADMIN.")
    fun grant(
        @PathVariable id: Long,
        @PathVariable role: String
    ): ResponseEntity<Void> = service.addRole(id, role)
        .let {
            if (it) ResponseEntity.ok().build()
            else ResponseEntity.noContent().build()
        }
}
