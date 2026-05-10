package br.pucpr.authserver.users.dtos.requests

import br.pucpr.authserver.users.entities.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CreateUserRequest(
    @Email
    val email: String?,

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}\$")
    val password: String?,

    @NotBlank
    val name: String?
) {
    fun toUser() = User(email = email!!, password = password!!, name = name!!)
}
