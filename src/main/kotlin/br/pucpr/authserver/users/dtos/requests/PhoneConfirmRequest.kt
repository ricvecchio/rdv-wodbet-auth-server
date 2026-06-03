package br.pucpr.authserver.users.dtos.requests

import jakarta.validation.constraints.NotBlank

data class PhoneConfirmRequest(
    @field:NotBlank
    val phone: String?,

    @field:NotBlank
    val uuid: String?,

    @field:NotBlank
    val code: String?
)

