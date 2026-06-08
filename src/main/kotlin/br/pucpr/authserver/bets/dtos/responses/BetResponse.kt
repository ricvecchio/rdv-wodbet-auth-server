package br.pucpr.authserver.bets.dtos.responses

import br.pucpr.authserver.bets.entities.Bet
import br.pucpr.authserver.bets.entities.BetVote

data class BetResponse(
    val id: String,
    val createdAt: String?,
    val updatedAt: String?,
    val createdByUserId: String,
    val athleteAUserId: String,
    val athleteBUserId: String,
    val wodTitle: String,
    val prizeType: String,
    val prizeOtherDescription: String?,
    val status: String,
    val expiresAt: String,
    val proposedWinnerUserId: String?,
    val athleteAConfirmed: Boolean,
    val athleteBConfirmed: Boolean,
    val confirmedWinnerUserId: String?,
    val votesByUserId: Map<String, String>,
    val athleteAResult: String?,
    val athleteBResult: String?
) {
    constructor(bet: Bet, votes: List<BetVote> = emptyList()) : this(
        id = bet.id!!.toString(),
        createdAt = bet.createdAt?.toString(),
        updatedAt = bet.updatedAt?.toString(),
        createdByUserId = bet.createdByUserId.toString(),
        athleteAUserId = bet.athleteAUserId.toString(),
        athleteBUserId = bet.athleteBUserId.toString(),
        wodTitle = bet.wodTitle,
        prizeType = bet.prizeType.jsonValue,
        prizeOtherDescription = bet.prizeOtherDescription,
        status = bet.status.jsonValue,
        expiresAt = bet.expiresAt.toString(),
        proposedWinnerUserId = bet.proposedWinnerUserId?.toString(),
        athleteAConfirmed = bet.athleteAConfirmed,
        athleteBConfirmed = bet.athleteBConfirmed,
        confirmedWinnerUserId = bet.confirmedWinnerUserId?.toString(),
        votesByUserId = votes.associate { it.voterUserId.toString() to it.votedAthleteUserId.toString() },
        athleteAResult = bet.athleteAResult,
        athleteBResult = bet.athleteBResult
    )
}

