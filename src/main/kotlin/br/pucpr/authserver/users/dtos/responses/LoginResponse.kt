package br.pucpr.authserver.users.dtos.responses

data class LoginResponse(
    val token: String,
    val user: UserResponse
)
