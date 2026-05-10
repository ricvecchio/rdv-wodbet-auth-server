package br.pucpr.authserver

import br.pucpr.authserver.roles.entities.Role
import br.pucpr.authserver.roles.repositories.RoleRepository
import br.pucpr.authserver.users.entities.User
import br.pucpr.authserver.users.repositories.UserRepository
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
class Bootstrapper(
    val rolesRepository: RoleRepository,
    val userRepository: UserRepository
) : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        rolesRepository.findByName("USER") ?: rolesRepository
            .save(Role(name = "USER", description = "Standard user"))
        val adminRole =
            rolesRepository.findByName("ADMIN") ?: rolesRepository
                .save(Role(name = "ADMIN", description = "System Administrator"))

        if (userRepository.findByRole("ADMIN").isEmpty()) {
            val admin = User(
                email = "admin@authserver.com",
                password = "admin",
                name = "Auth Server Administrator",
            )
            admin.roles.add(adminRole)
            userRepository.save(admin)
        }
    }
}
