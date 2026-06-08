package br.pucpr.authserver.users.dtos.requests

data class UpdateUserProfileRequest(
    val name: String?,
    val description: String?,
    val phone: String?,
    val photoUrl: String?
)

