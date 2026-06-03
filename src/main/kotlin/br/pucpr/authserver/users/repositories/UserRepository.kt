package br.pucpr.authserver.users.repositories

import br.pucpr.authserver.users.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByPhone(phone: String): User?
    fun findByPhoneAndUuid(phone: String, uuid: String): User?

    @Query(
        """
            select distinct u from User u
            join u.roles r
            where r.name = :role
            order by u.name
        """
    )
    fun findByRole(role: String): List<User>
}
