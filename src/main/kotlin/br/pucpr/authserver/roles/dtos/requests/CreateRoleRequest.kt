package br.pucpr.authserver.roles.dtos.requests

import br.pucpr.authserver.roles.entities.Role
import jakarta.validation.constraints.NotBlank

data class CreateRoleRequest(
    @NotBlank
    val name: String?,

    @NotBlank
    val description: String?
) {
    fun toRole() = Role(name = name!!, description = description!!)
}
