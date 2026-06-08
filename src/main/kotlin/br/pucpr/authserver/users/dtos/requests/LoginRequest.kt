package br.pucpr.authserver.users.dtos.requests

data class LoginRequest(
    val email: String? = null,
    val password: String? = null,
    val phone: String? = null,
    val uuid: String? = null
)
