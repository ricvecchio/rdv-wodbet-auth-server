package br.pucpr.authserver.users.services

import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.exceptions.UnauthorizedException
import br.pucpr.authserver.roles.repositories.RoleRepository
import br.pucpr.authserver.security.Jwt
import br.pucpr.authserver.users.dtos.responses.LoginResponse
import br.pucpr.authserver.users.dtos.responses.UserResponse
import br.pucpr.authserver.users.entities.User
import br.pucpr.authserver.users.enums.SortDir
import br.pucpr.authserver.users.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(
    val repository: UserRepository,
    val roleRepository: RoleRepository,
    val jwt: Jwt,
    val confirmationCodeService: ConfirmationCodeService
) {
    fun insert(user: User): User {
        if (repository.findByEmail(user.email) != null) {
            throw BadRequestException("User already exists")
        }
        val userRole = roleRepository.findByName("USER")
            ?: throw BadRequestException("Role USER not found")
        user.roles.add(userRole)
        return repository.save(user)
    }

    fun findAll(dir: SortDir = SortDir.ASC) = when (dir) {
        SortDir.ASC -> repository.findAll(Sort.by("name").ascending())
        SortDir.DESC -> repository.findAll(Sort.by("name").descending())
    }

    fun findActiveUsers() = repository.findAllByActiveTrueOrderByNameAsc()

    fun findByIdOrNull(id: Long) = repository.findByIdOrNull(id)
    fun findById(id: Long) = repository.findByIdOrNull(id) ?: throw NotFoundException(id)

    fun delete(id: Long) {
        val user = findById(id)
        if (user.isAdmin() && repository.findByRole("ADMIN").size == 1) {
            throw BadRequestException("Cannot delete the last admin")
        }
        repository.delete(user)
        log.info("User ${id} deleted successfully")
    }

    fun findByRole(role: String) = repository.findByRole(role)

    fun addRole(id: Long, roleName: String): Boolean {
        val upperRole = roleName.uppercase()
        val user = findById(id)
        if (user.roles.any { it.name == upperRole }) return false
        val role = roleRepository.findByName(upperRole) ?: throw BadRequestException("Role $upperRole not found")
        user.roles.add(role)
        repository.save(user)
        log.info("User ${id} successfully added to role $role")
        return true
    }

    fun update(id: Long, name: String): User? {
        val user = findById(id)
        if (user.name == name) return null
        user.name = name
        repository.save(user)
        return user
    }

    fun login(email: String, password: String): LoginResponse {
        val user = repository.findByEmail(email) ?: throw UnauthorizedException("User $email not found")
        if (user.password != password) throw UnauthorizedException("Invalid password")
        log.info("User ${user.id} is logged in")
        return LoginResponse(token = jwt.createToken(user), UserResponse(user))
    }

    // ── Phone-based login (iOS flow) ────────────────────────────────────────────

    /**
     * Returns the existing active user (200) or generates a confirmation code (202).
     * Returns null when a code was sent (202 case); returns the User when already authenticated (200 case).
     */
    fun phoneLogin(phone: String, uuid: String): User? {
        val normalizedPhone = normalizePhone(phone)
        val existingUser = repository.findByPhoneAndUuid(normalizedPhone, uuid)
        if (existingUser != null && existingUser.active) {
            log.info("Phone login: user ${existingUser.id} already authenticated with phone=$normalizedPhone uuid=$uuid")
            return existingUser
        }
        // User not found or uuid changed → generate new code
        confirmationCodeService.generateAndSend(normalizedPhone, uuid)
        return null
    }

    /**
     * Validates the confirmation code, then creates or updates the user.
     */
    fun confirmPhone(phone: String, uuid: String, code: String): User {
        val normalizedPhone = normalizePhone(phone)
        confirmationCodeService.validateAndConsume(normalizedPhone, uuid, code)

        val existingUser = repository.findByPhone(normalizedPhone)
        return if (existingUser != null) {
            existingUser.uuid = uuid
            existingUser.active = true
            repository.save(existingUser)
        } else {
            val newUser = User(
                email = "${normalizedPhone}@phone.local",
                password = "",
                name = "",
                phone = normalizedPhone,
                uuid = uuid,
                active = true
            )
            roleRepository.findByName("USER")?.let { newUser.roles.add(it) }
            repository.save(newUser)
        }
    }

    /**
     * Updates user profile (name, description, phone, photoUrl).
     */
    fun updateProfile(id: Long, name: String?, description: String?, phone: String?, photoUrl: String?): User {
        val user = findById(id)
        if (!name.isNullOrBlank()) user.name = name
        if (description != null) user.description = description
        if (phone != null) user.phone = normalizePhone(phone)
        if (photoUrl != null) user.photoUrl = photoUrl
        return repository.save(user)
    }

    private fun normalizePhone(phone: String) = phone.replace(Regex("[^0-9]"), "")

    companion object {
        val log = LoggerFactory.getLogger(UserService::class.java)
    }
}
