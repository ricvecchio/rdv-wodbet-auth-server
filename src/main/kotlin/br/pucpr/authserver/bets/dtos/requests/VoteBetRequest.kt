package br.pucpr.authserver.bets.dtos.requests

import jakarta.validation.constraints.NotBlank

data class VoteBetRequest(
    @field:NotBlank
    val voterUserId: String?,

    @field:NotBlank
    val votedAthleteUserId: String?
)

