package br.pucpr.authserver.bets.repositories

import br.pucpr.authserver.bets.entities.Bet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BetRepository : JpaRepository<Bet, Long>

