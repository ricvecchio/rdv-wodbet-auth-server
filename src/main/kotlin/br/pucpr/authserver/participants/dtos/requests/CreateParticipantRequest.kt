package br.pucpr.authserver.participants.dtos.requests

import br.pucpr.authserver.participants.entities.Participant
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateParticipantRequest(
    @field:NotBlank val name: String?,
    @field:NotBlank @field:Email val email: String?,
    val phone: String? = null
) {
    fun toParticipant() = Participant(name = name!!, email = email!!, phone = phone ?: "")
}
