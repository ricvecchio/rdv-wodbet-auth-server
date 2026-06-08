package br.pucpr.authserver.users.dtos.responses

import br.pucpr.authserver.users.entities.User
import com.fasterxml.jackson.annotation.JsonFormat

data class BackendUserResponse(
    val id: String,
    val name: String,
    val phone: String?,
    val uuid: String?,
    val active: Boolean,
    val description: String?,
    val photoUrl: String?,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    val createdAt: String?
) {
    constructor(user: User) : this(
        id = user.id!!.toString(),
        name = user.displayName ?: user.name,
        phone = user.phone,
        uuid = user.uuid,
        active = user.active,
        description = user.description,
        photoUrl = user.photoUrl,
        createdAt = user.createdAt?.toString()?.plus("Z")
    )
}

