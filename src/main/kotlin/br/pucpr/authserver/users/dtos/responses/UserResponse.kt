package br.pucpr.authserver.users.dtos.responses

import br.pucpr.authserver.users.entities.User

data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val phone: String?,
    val uuid: String?,
    val active: Boolean,
    val description: String?,
    val photoUrl: String?,
    val createdAt: String?,
    val roles: List<String>
) {
    constructor(user: User) : this(
        id = user.id!!.toString(),
        email = user.email,
        name = user.displayName ?: user.name,
        phone = user.phone,
        uuid = user.uuid,
        active = user.active,
        description = user.description,
        photoUrl = user.photoUrl,
        createdAt = user.createdAt?.toString()?.plus("Z"),
        roles = user.roles.map { it.name }.sorted()
    )
}
