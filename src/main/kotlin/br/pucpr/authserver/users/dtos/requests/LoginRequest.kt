package br.pucpr.authserver.users.dtos.requests

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @NotBlank
    var email: String?,

    @NotBlank
    var password: String?
)
