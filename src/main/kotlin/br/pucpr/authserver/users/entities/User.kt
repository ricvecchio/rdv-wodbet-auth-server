package br.pucpr.authserver.users.entities

import br.pucpr.authserver.roles.entities.Role
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "UserTable")
class User(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false)
    var email: String,

    var password: String,
    var name: String = "",

    var phone: String? = null,
    var uuid: String? = null,
    var active: Boolean = true,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    var createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToMany
    @JoinTable(
        name = "UserRole",
        joinColumns = [JoinColumn(name = "idUser")],
        inverseJoinColumns = [JoinColumn(name = "idRole")]
    )
    var roles: MutableSet<Role> = mutableSetOf()
) {
    @Transient
    fun isAdmin() = roles.any { it.name == "ADMIN" }
}
