package br.pucpr.authserver.participants.entities

import br.pucpr.authserver.events.entities.Event
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "participants")
class Participant(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    var phone: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    var event: Event? = null,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)
