package br.pucpr.authserver.users.entities

import br.pucpr.authserver.roles.entities.Role
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "UserTable")
class User(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var name: String = "",

    @Column(name = "display_name")
    var displayName: String? = null,

    var phone: String? = null,
    var uuid: String? = null,
    var active: Boolean = true,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "photo_url")
    var photoUrl: String? = null,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null,

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
