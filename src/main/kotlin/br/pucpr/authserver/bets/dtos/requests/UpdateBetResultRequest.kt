package br.pucpr.authserver.bets.dtos.requests

import jakarta.validation.constraints.NotBlank

data class UpdateBetResultRequest(
    @field:NotBlank
    val requesterUserId: String?,

    val athleteAResult: String?,
    val athleteBResult: String?,

    @field:NotBlank
    val winnerUserId: String?
)

