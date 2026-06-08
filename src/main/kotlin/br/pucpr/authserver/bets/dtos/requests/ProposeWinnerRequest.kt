package br.pucpr.authserver.bets.dtos.requests

import jakarta.validation.constraints.NotBlank

data class ProposeWinnerRequest(
    @field:NotBlank
    val requesterUserId: String?,

    @field:NotBlank
    val proposedWinnerUserId: String?
)

