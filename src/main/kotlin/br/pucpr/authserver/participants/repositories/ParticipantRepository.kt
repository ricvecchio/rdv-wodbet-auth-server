package br.pucpr.authserver.participants.repositories

import br.pucpr.authserver.participants.entities.Participant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ParticipantRepository : JpaRepository<Participant, Long> {
    fun findByEmail(email: String): Participant?
    fun findByEventId(eventId: Long): List<Participant>
}
