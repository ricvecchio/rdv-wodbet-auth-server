package br.pucpr.authserver.participants.services

import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.participants.dtos.requests.UpdateParticipantRequest
import br.pucpr.authserver.participants.entities.Participant
import br.pucpr.authserver.participants.exceptions.ParticipantNotFoundException
import br.pucpr.authserver.participants.repositories.ParticipantRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ParticipantService(private val repository: ParticipantRepository) {
    companion object {
        private val log = LoggerFactory.getLogger(ParticipantService::class.java)
    }

    @Transactional
    fun create(participant: Participant): Participant {
        if (repository.findByEmail(participant.email) != null) {
            throw BadRequestException("Participant with email '${participant.email}' already exists")
        }
        val saved = repository.save(participant)
        log.info("Participant created: id=${saved.id}, email=${saved.email}")
        return saved
    }

    @Transactional(readOnly = true)
    fun findAll(sortDir: String = "ASC"): List<Participant> {
        val sort = if (sortDir.uppercase() == "DESC") Sort.by("name").descending()
        else Sort.by("name").ascending()
        return repository.findAll(sort)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Participant =
        repository.findByIdOrNull(id) ?: run {
            log.warn("Participant not found: id={}", id)
            throw ParticipantNotFoundException(id)
        }

    @Transactional
    fun update(id: Long, request: UpdateParticipantRequest): Participant {
        val participant = findById(id)
        var changed = false
        request.name?.let { if (participant.name != it) { participant.name = it; changed = true } }
        request.phone?.let { if (participant.phone != it) { participant.phone = it; changed = true } }
        return if (changed) {
            val updated = repository.save(participant)
            log.info("Participant updated: id={}", updated.id)
            updated
        } else participant
    }

    @Transactional
    fun delete(id: Long) {
        repository.delete(findById(id))
        log.info("Participant deleted: id={}", id)
    }
}
