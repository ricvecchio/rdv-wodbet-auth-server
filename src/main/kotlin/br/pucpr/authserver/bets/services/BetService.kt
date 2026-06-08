package br.pucpr.authserver.bets.services

import br.pucpr.authserver.bets.dtos.requests.CreateBetRequest
import br.pucpr.authserver.bets.dtos.requests.ProposeWinnerRequest
import br.pucpr.authserver.bets.dtos.requests.UpdateBetResultRequest
import br.pucpr.authserver.bets.dtos.requests.VoteBetRequest
import br.pucpr.authserver.bets.dtos.responses.BetResponse
import br.pucpr.authserver.bets.entities.Bet
import br.pucpr.authserver.bets.entities.BetVote
import br.pucpr.authserver.bets.enums.BetStatus
import br.pucpr.authserver.bets.enums.PrizeType
import br.pucpr.authserver.bets.repositories.BetRepository
import br.pucpr.authserver.bets.repositories.BetVoteRepository
import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.users.services.UserService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class BetService(
    private val betRepository: BetRepository,
    private val voteRepository: BetVoteRepository,
    private val userService: UserService
) {
    companion object {
        private val log = LoggerFactory.getLogger(BetService::class.java)
    }

    @Transactional
    fun create(request: CreateBetRequest): BetResponse {
        val bet = Bet(
            createdByUserId = parseUserId(request.createdByUserId, "createdByUserId"),
            athleteAUserId = parseUserId(request.athleteAUserId, "athleteAUserId"),
            athleteBUserId = parseUserId(request.athleteBUserId, "athleteBUserId"),
            wodTitle = request.wodTitle?.trim().orEmpty(),
            prizeType = parsePrizeType(request.prizeType),
            prizeOtherDescription = request.prizeOtherDescription,
            expiresAt = parseInstant(request.expiresAt, "expiresAt")
        )
        validateCreate(bet)
        userService.findById(bet.createdByUserId)
        userService.findById(bet.athleteAUserId)
        userService.findById(bet.athleteBUserId)
        val saved = betRepository.save(bet)
        log.info("Bet created: id=${saved.id}")
        return BetResponse(saved)
    }

    @Transactional
    fun findAll(): List<BetResponse> = betRepository.findAll(Sort.by("createdAt").descending())
        .map { expireIfNeeded(it) }
        .map { toResponse(it) }

    @Transactional
    fun findById(id: Long): BetResponse = toResponse(expireIfNeeded(findBet(id)))

    @Transactional
    fun proposeWinner(id: Long, request: ProposeWinnerRequest): BetResponse {
        val bet = requireMutableBet(id)
        ensureNotClosed(bet)
        val requesterId = parseUserId(request.requesterUserId, "requesterUserId")
        val proposedWinnerId = parseUserId(request.proposedWinnerUserId, "proposedWinnerUserId")
        ensureAllowedRequester(bet, requesterId)
        ensureAthleteChoice(bet, proposedWinnerId)
        bet.proposedWinnerUserId = proposedWinnerId
        bet.athleteAConfirmed = false
        bet.athleteBConfirmed = false
        bet.confirmedWinnerUserId = null
        bet.status = BetStatus.OPEN
        val saved = betRepository.save(bet)
        log.info("Bet {} proposed winner updated to {}", id, proposedWinnerId)
        return toResponse(saved)
    }

    @Transactional
    fun confirmWinner(id: Long, confirmerUserId: String): BetResponse {
        val bet = requireMutableBet(id)
        ensureNotClosed(bet)
        if (bet.proposedWinnerUserId == null) throw BadRequestException("No proposed winner")
        val confirmerId = parseUserId(confirmerUserId, "confirmerUserId")
        ensureAthleteParticipant(bet, confirmerId)
        when (confirmerId) {
            bet.athleteAUserId -> bet.athleteAConfirmed = true
            bet.athleteBUserId -> bet.athleteBConfirmed = true
        }
        if (bet.athleteAConfirmed && bet.athleteBConfirmed) {
            bet.status = BetStatus.FINISHED
            bet.confirmedWinnerUserId = bet.proposedWinnerUserId
        }
        val saved = betRepository.save(bet)
        return toResponse(saved)
    }

    @Transactional
    fun rejectWinner(id: Long, rejectorUserId: String): BetResponse {
        val bet = requireMutableBet(id)
        ensureNotClosed(bet)
        val rejectorId = parseUserId(rejectorUserId, "rejectorUserId")
        ensureAthleteParticipant(bet, rejectorId)
        bet.status = BetStatus.DISPUTED
        bet.proposedWinnerUserId = null
        bet.confirmedWinnerUserId = null
        bet.athleteAConfirmed = false
        bet.athleteBConfirmed = false
        val saved = betRepository.save(bet)
        return toResponse(saved)
    }

    @Transactional
    fun cancel(id: Long, requesterUserId: String): BetResponse {
        val bet = requireMutableBet(id)
        ensureCancelable(bet)
        val requesterId = parseUserId(requesterUserId, "requesterUserId")
        if (requesterId != bet.createdByUserId) throw BadRequestException("Only creator can cancel bet")
        bet.status = BetStatus.CANCELED
        val saved = betRepository.save(bet)
        return toResponse(saved)
    }

    @Transactional
    fun updateResult(id: Long, request: UpdateBetResultRequest): BetResponse {
        val bet = requireMutableBet(id)
        val requesterId = parseUserId(request.requesterUserId, "requesterUserId")
        ensureAllowedRequester(bet, requesterId)
        val winnerId = parseUserId(request.winnerUserId, "winnerUserId")
        ensureAthleteChoice(bet, winnerId)
        bet.athleteAResult = request.athleteAResult
        bet.athleteBResult = request.athleteBResult
        bet.proposedWinnerUserId = winnerId
        bet.confirmedWinnerUserId = winnerId
        bet.athleteAConfirmed = true
        bet.athleteBConfirmed = true
        bet.status = BetStatus.FINISHED
        val saved = betRepository.save(bet)
        return toResponse(saved)
    }

    @Transactional
    fun vote(id: Long, request: VoteBetRequest): BetResponse {
        val bet = requireMutableBet(id)
        val voterId = parseUserId(request.voterUserId, "voterUserId")
        val votedAthleteId = parseUserId(request.votedAthleteUserId, "votedAthleteUserId")
        if (bet.status !in setOf(BetStatus.OPEN, BetStatus.DISPUTED)) {
            throw BadRequestException("Bet is not open for voting")
        }
        ensureAthleteChoice(bet, votedAthleteId)
        userService.findById(voterId)
        userService.findById(votedAthleteId)
        voteRepository.findByBetIdAndVoterUserId(id, voterId)?.let { voteRepository.delete(it) }
        voteRepository.save(BetVote(betId = id, voterUserId = voterId, votedAthleteUserId = votedAthleteId))
        return toResponse(expireIfNeeded(bet))
    }

    @Transactional
    fun seedBet(bet: Bet, votes: List<BetVote> = emptyList()): Bet {
        val saved = betRepository.save(bet)
        votes.forEach { voteRepository.save(it.copyWithBet(saved.id!!)) }
        return saved
    }

    private fun findBet(id: Long): Bet = betRepository.findByIdOrNull(id) ?: throw NotFoundException(id)

    private fun requireMutableBet(id: Long): Bet = expireIfNeeded(findBet(id))

    private fun expireIfNeeded(bet: Bet): Bet {
        if (bet.status == BetStatus.OPEN && bet.expiresAt.isBefore(Instant.now())) {
            bet.status = BetStatus.EXPIRED
            return betRepository.save(bet)
        }
        return bet
    }

    private fun toResponse(bet: Bet): BetResponse = BetResponse(bet, voteRepository.findAllByBetId(bet.id!!))

    private fun validateCreate(bet: Bet) {
        if (bet.athleteAUserId == bet.athleteBUserId) throw BadRequestException("Athletes must be different users")
        if (bet.wodTitle.isBlank()) throw BadRequestException("wodTitle is required")
        if (bet.prizeType == PrizeType.OTHER && bet.prizeOtherDescription.isNullOrBlank()) {
            throw BadRequestException("prizeOtherDescription is required when prizeType is other")
        }
        if (!bet.expiresAt.isAfter(Instant.now())) throw BadRequestException("expiresAt must be in the future")
    }

    private fun ensureNotClosed(bet: Bet) {
        if (bet.status in setOf(BetStatus.FINISHED, BetStatus.CANCELED, BetStatus.EXPIRED)) {
            throw BadRequestException("Bet is closed")
        }
    }

    private fun ensureCancelable(bet: Bet) {
        if (bet.status in setOf(BetStatus.FINISHED, BetStatus.CANCELED, BetStatus.EXPIRED)) {
            throw BadRequestException("Bet cannot be canceled")
        }
    }

    private fun ensureAllowedRequester(bet: Bet, requesterId: Long) {
        if (requesterId != bet.createdByUserId && requesterId != bet.athleteAUserId && requesterId != bet.athleteBUserId) {
            throw BadRequestException("User is not allowed to perform this action")
        }
    }

    private fun ensureAthleteParticipant(bet: Bet, userId: Long) {
        if (userId != bet.athleteAUserId && userId != bet.athleteBUserId) {
            throw BadRequestException("Only bet athletes can perform this action")
        }
    }

    private fun ensureAthleteChoice(bet: Bet, userId: Long) {
        if (userId != bet.athleteAUserId && userId != bet.athleteBUserId) {
            throw BadRequestException("winner/vote must be one of the bet athletes")
        }
    }

    private fun parseUserId(value: String?, field: String): Long = value?.toLongOrNull()
        ?: throw BadRequestException("$field is invalid")

    private fun parsePrizeType(value: String?): PrizeType = value?.let { PrizeType.from(it) }
        ?: throw BadRequestException("prizeType is required")

    private fun parseInstant(value: String?, field: String): Instant = try {
        Instant.parse(value)
    } catch (_: Exception) {
        throw BadRequestException("$field is invalid")
    }

    private fun BetVote.copyWithBet(betId: Long) = BetVote(
        betId = betId,
        voterUserId = voterUserId,
        votedAthleteUserId = votedAthleteUserId
    )
}

