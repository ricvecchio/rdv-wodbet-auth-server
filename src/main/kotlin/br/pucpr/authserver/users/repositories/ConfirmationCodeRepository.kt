package br.pucpr.authserver.users.repositories

import br.pucpr.authserver.users.entities.ConfirmationCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConfirmationCodeRepository : JpaRepository<ConfirmationCode, Long> {
    fun findTopByPhoneAndUuidAndUsedFalseOrderByCreatedAtDesc(phone: String, uuid: String): ConfirmationCode?
}

