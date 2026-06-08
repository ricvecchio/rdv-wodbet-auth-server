package br.pucpr.authserver.bets.entities

import br.pucpr.authserver.bets.enums.BetStatus
import br.pucpr.authserver.bets.enums.PrizeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(name = "bets")
class Bet(
    @Id @GeneratedValue
    var id: Long? = null,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: Instant? = null,

    @Column(nullable = false)
    var createdByUserId: Long,

    @Column(nullable = false)
    var athleteAUserId: Long,

    @Column(nullable = false)
    var athleteBUserId: Long,

    @Column(nullable = false)
    var wodTitle: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var prizeType: PrizeType,

    @Column(columnDefinition = "TEXT")
    var prizeOtherDescription: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BetStatus = BetStatus.OPEN,

    @Column(nullable = false)
    var expiresAt: Instant,

    var proposedWinnerUserId: Long? = null,

    @Column(nullable = false)
    var athleteAConfirmed: Boolean = false,

    @Column(nullable = false)
    var athleteBConfirmed: Boolean = false,

    var confirmedWinnerUserId: Long? = null,

    @Column(columnDefinition = "TEXT")
    var athleteAResult: String? = null,

    @Column(columnDefinition = "TEXT")
    var athleteBResult: String? = null,
)

