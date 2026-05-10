package br.pucpr.authserver.users.dtos.responses

import br.pucpr.authserver.users.entities.User

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val roles: List<String>
) {
    constructor(user: User) : this(
        id = user.id!!,
        email = user.email,
        name = user.name,
        roles = user.roles.map { it.name }.sorted()
    )
}
