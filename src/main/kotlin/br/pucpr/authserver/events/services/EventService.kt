package br.pucpr.authserver.events.services

import br.pucpr.authserver.events.dtos.requests.UpdateEventRequest
import br.pucpr.authserver.events.entities.Event
import br.pucpr.authserver.events.enums.EventStatus
import br.pucpr.authserver.events.exceptions.EventNotFoundException
import br.pucpr.authserver.events.repositories.EventRepository
import br.pucpr.authserver.participants.entities.Participant
import br.pucpr.authserver.participants.exceptions.ParticipantAlreadyLinkedException
import br.pucpr.authserver.participants.exceptions.ParticipantNotFoundException
import br.pucpr.authserver.participants.exceptions.ParticipantNotLinkedException
import br.pucpr.authserver.participants.repositories.ParticipantRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val participantRepository: ParticipantRepository
) {
    companion object {
        private val log = LoggerFactory.getLogger(EventService::class.java)
    }

    @Transactional
    fun create(event: Event): Event {
        val saved = eventRepository.save(event)
        log.info("Event created: id=${saved.id}, name=${saved.name}")
        return saved
    }

    @Transactional(readOnly = true)
    fun findAll(
        name: String? = null,
        status: EventStatus? = null,
        location: String? = null,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        sortBy: String = "eventDate",
        direction: String = "ASC"
    ): List<Event> {
        val sortField = when (sortBy.lowercase()) {
            "name" -> "name"
            "location" -> "location"
            "createdat" -> "createdAt"
            else -> "eventDate"
        }
        val sort = if (direction.uppercase() == "DESC") Sort.by(sortField).descending()
        else Sort.by(sortField).ascending()
        return eventRepository.findWithFilters(name, status, location, startDate, endDate, sort)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Event =
        eventRepository.findByIdOrNull(id) ?: run {
            log.warn("Event not found: id={}", id)
            throw EventNotFoundException(id)
        }

    @Transactional
    fun update(id: Long, request: UpdateEventRequest): Event {
        val event = findById(id)
        var changed = false
        request.name?.let { if (event.name != it) { event.name = it; changed = true } }
        request.description?.let { if (event.description != it) { event.description = it; changed = true } }
        request.location?.let { if (event.location != it) { event.location = it; changed = true } }
        request.eventDate?.let { if (event.eventDate != it) { event.eventDate = it; changed = true } }
        request.status?.let { if (event.status != it) { event.status = it; changed = true } }
        return if (changed) {
            val updated = eventRepository.save(event)
            log.info("Event updated: id=${updated.id}, name=${updated.name}")
            updated
        } else event
    }

    @Transactional
    fun delete(id: Long) {
        eventRepository.delete(findById(id))
        log.info("Event deleted: id={}", id)
    }

    @Transactional
    fun addParticipant(eventId: Long, participantId: Long): Event {
        val event = findById(eventId)
        val participant = participantRepository.findByIdOrNull(participantId)
            ?: run {
                log.warn("Participant not found while linking: participantId={}", participantId)
                throw ParticipantNotFoundException(participantId)
            }
        if (participant.event?.id == eventId) throw ParticipantAlreadyLinkedException(participantId, eventId)
        participant.event = event
        participantRepository.save(participant)
        log.info("Participant {} linked to event {}", participantId, eventId)
        return eventRepository.findByIdOrNull(eventId)!!
    }

    @Transactional
    fun removeParticipant(eventId: Long, participantId: Long) {
        findById(eventId)
        val participant = participantRepository.findByIdOrNull(participantId)
            ?: run {
                log.warn("Participant not found while unlinking: participantId={}", participantId)
                throw ParticipantNotFoundException(participantId)
            }
        if (participant.event?.id != eventId) throw ParticipantNotLinkedException(participantId, eventId)
        participant.event = null
        participantRepository.save(participant)
        log.info("Participant {} removed from event {}", participantId, eventId)
    }

    @Transactional(readOnly = true)
    fun listParticipants(eventId: Long): List<Participant> {
        findById(eventId)
        return participantRepository.findByEventId(eventId)
    }
}
