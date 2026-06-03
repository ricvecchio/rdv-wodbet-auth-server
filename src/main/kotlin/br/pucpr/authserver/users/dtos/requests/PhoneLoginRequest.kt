package br.pucpr.authserver.users.dtos.requests

import jakarta.validation.constraints.NotBlank

data class PhoneLoginRequest(
    @field:NotBlank
    val phone: String?,

    @field:NotBlank
    val uuid: String?
)

