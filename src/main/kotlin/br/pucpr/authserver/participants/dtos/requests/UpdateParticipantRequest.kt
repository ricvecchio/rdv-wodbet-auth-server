package br.pucpr.authserver.participants.dtos.requests

data class UpdateParticipantRequest(
    val name: String? = null,
    val phone: String? = null
)
