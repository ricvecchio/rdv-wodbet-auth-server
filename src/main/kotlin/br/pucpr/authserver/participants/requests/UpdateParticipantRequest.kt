package br.pucpr.authserver.participants.requests

data class UpdateParticipantRequest(
    val name: String? = null,
    val phone: String? = null
)

