package br.pucpr.authserver.bets.repositories

import br.pucpr.authserver.bets.entities.BetVote
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BetVoteRepository : JpaRepository<BetVote, Long> {
    fun findAllByBetId(betId: Long): List<BetVote>
    fun findByBetIdAndVoterUserId(betId: Long, voterUserId: Long): BetVote?
    fun deleteByBetIdAndVoterUserId(betId: Long, voterUserId: Long)
}

