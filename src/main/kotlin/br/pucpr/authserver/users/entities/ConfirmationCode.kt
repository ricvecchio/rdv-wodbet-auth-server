package br.pucpr.authserver.users.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "ConfirmationCode")
class ConfirmationCode(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false)
    var phone: String,

    @Column(nullable = false)
    var uuid: String,

    @Column(nullable = false)
    var code: String,

    @Column(nullable = false)
    var expiresAt: LocalDateTime,

    var used: Boolean = false,

    var createdAt: LocalDateTime = LocalDateTime.now()
)

