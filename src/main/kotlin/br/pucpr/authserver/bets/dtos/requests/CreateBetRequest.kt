package br.pucpr.authserver.bets.dtos.requests

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateBetRequest(
    @field:NotBlank
    val createdByUserId: String?,

    @field:NotBlank
    val athleteAUserId: String?,

    @field:NotBlank
    val athleteBUserId: String?,

    @field:NotBlank
    val wodTitle: String?,

    @field:NotBlank
    val prizeType: String?,

    val prizeOtherDescription: String?,

    @field:NotBlank
    val expiresAt: String?
)

