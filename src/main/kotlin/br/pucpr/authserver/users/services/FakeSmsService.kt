package br.pucpr.authserver.users.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FakeSmsService {
    fun sendCode(phone: String, code: String) {
        log.info("=== [FakeSMS] Sending confirmation code to phone $phone: CODE = $code ===")
    }

    companion object {
        private val log = LoggerFactory.getLogger(FakeSmsService::class.java)
    }
}

