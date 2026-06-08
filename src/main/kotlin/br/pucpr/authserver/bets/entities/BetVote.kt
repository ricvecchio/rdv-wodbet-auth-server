package br.pucpr.authserver.bets.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(
    name = "bet_votes",
    uniqueConstraints = [UniqueConstraint(columnNames = ["bet_id", "voter_user_id"])]
)
class BetVote(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(name = "bet_id", nullable = false)
    var betId: Long,

    @Column(name = "voter_user_id", nullable = false)
    var voterUserId: Long,

    @Column(name = "voted_athlete_user_id", nullable = false)
    var votedAthleteUserId: Long,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: Instant? = null,
)

