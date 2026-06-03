package br.pucpr.authserver.users.services

import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.users.entities.ConfirmationCode
import br.pucpr.authserver.users.repositories.ConfirmationCodeRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ConfirmationCodeService(
    private val repository: ConfirmationCodeRepository,
    private val fakeSmsService: FakeSmsService
) {
    fun generateAndSend(phone: String, uuid: String): ConfirmationCode {
        val code = (100000..999999).random().toString()
        val confirmation = ConfirmationCode(
            phone = phone,
            uuid = uuid,
            code = code,
            expiresAt = LocalDateTime.now().plusMinutes(10)
        )
        val saved = repository.save(confirmation)
        fakeSmsService.sendCode(phone, code)
        log.info("Confirmation code generated for phone=$phone, uuid=$uuid")
        return saved
    }

    fun validateAndConsume(phone: String, uuid: String, code: String): ConfirmationCode {
        val confirmation = repository.findTopByPhoneAndUuidAndUsedFalseOrderByCreatedAtDesc(phone, uuid)
            ?: throw NotFoundException("No pending confirmation code for phone=$phone and uuid=$uuid")

        if (confirmation.expiresAt.isBefore(LocalDateTime.now())) {
            throw BadRequestException("Confirmation code has expired.")
        }
        if (confirmation.code != code) {
            throw BadRequestException("Invalid confirmation code.")
        }

        confirmation.used = true
        repository.save(confirmation)
        return confirmation
    }

    companion object {
        private val log = LoggerFactory.getLogger(ConfirmationCodeService::class.java)
    }
}

