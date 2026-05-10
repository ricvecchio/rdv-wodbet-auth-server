package br.pucpr.authserver.roles.dtos.responses

import br.pucpr.authserver.roles.entities.Role

data class RoleResponse(
    val name: String,
    val description: String,
) {
    constructor(role: Role) : this(role.name, role.description)
}
