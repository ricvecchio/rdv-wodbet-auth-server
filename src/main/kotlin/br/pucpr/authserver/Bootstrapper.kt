package br.pucpr.authserver

import br.pucpr.authserver.bets.entities.Bet
import br.pucpr.authserver.bets.entities.BetVote
import br.pucpr.authserver.bets.enums.BetStatus
import br.pucpr.authserver.bets.enums.PrizeType
import br.pucpr.authserver.bets.services.BetService
import br.pucpr.authserver.roles.entities.Role
import br.pucpr.authserver.roles.repositories.RoleRepository
import br.pucpr.authserver.users.entities.User
import br.pucpr.authserver.users.repositories.UserRepository
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class Bootstrapper(
    val rolesRepository: RoleRepository,
    val userRepository: UserRepository,
    val betService: BetService,
    val environment: Environment
) : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        if (!environment.activeProfiles.any { it.equals("local", true) || it.equals("dev", true) }) return

        rolesRepository.findByName("USER") ?: rolesRepository
            .save(Role(name = "USER", description = "Standard user"))
        val adminRole =
            rolesRepository.findByName("ADMIN") ?: rolesRepository
                .save(Role(name = "ADMIN", description = "System Administrator"))

        if (userRepository.findByRole("ADMIN").isEmpty()) {
            val admin = User(
                email = "admin@authserver.com",
                password = "admin",
                name = "Auth Server Administrator",
            )
            admin.roles.add(adminRole)
            userRepository.save(admin)
        }

        seedUsersAndBets()
    }

    private fun seedUsersAndBets() {
        if (userRepository.findAllByActiveTrueOrderByNameAsc().size < 5) {
            val userRole = rolesRepository.findByName("USER")!!
            val fixtures = listOf(
                User(email = "ricardo@phone.local", password = "", name = "Ricardo", displayName = "Ricardo", phone = "11999999991", uuid = "uuid-ricardo", active = true, description = "Atleta do box"),
                User(email = "bruno@phone.local", password = "", name = "Bruno", displayName = "Bruno", phone = "11999999992", uuid = "uuid-bruno", active = true, description = "Atleta de teste"),
                User(email = "felipe@phone.local", password = "", name = "Felipe", displayName = "Felipe", phone = "11999999993", uuid = "uuid-felipe", active = true, description = "Atleta de teste"),
                User(email = "marina@phone.local", password = "", name = "Marina", displayName = "Marina", phone = "11999999994", uuid = "uuid-marina", active = true, description = "Atleta de teste"),
                User(email = "camila@phone.local", password = "", name = "Camila", displayName = "Camila", phone = "11999999995", uuid = "uuid-camila", active = true, description = "Atleta de teste")
            )
            fixtures.forEach { user ->
                if (userRepository.findByPhone(user.phone!!) == null) {
                    user.roles.add(userRole)
                    userRepository.save(user)
                }
            }
        }

        if (betService.findAll().isEmpty()) {
            val users = userRepository.findAllByActiveTrueOrderByNameAsc().associateBy { it.name }
            val ricardo = users["Ricardo"]!!.id!!
            val bruno = users["Bruno"]!!.id!!
            val felipe = users["Felipe"]!!.id!!
            val marina = users["Marina"]!!.id!!
            val camila = users["Camila"]!!.id!!

            betService.seedBet(
                Bet(
                    createdByUserId = ricardo,
                    athleteAUserId = bruno,
                    athleteBUserId = felipe,
                    wodTitle = "Fran 21-15-9",
                    prizeType = PrizeType.GATORADE,
                    expiresAt = Instant.now().plusSeconds(86400),
                    status = BetStatus.OPEN
                ),
                listOf(BetVote(betId = 0, voterUserId = marina, votedAthleteUserId = bruno))
            )

            betService.seedBet(
                Bet(
                    createdByUserId = ricardo,
                    athleteAUserId = marina,
                    athleteBUserId = camila,
                    wodTitle = "Open 24.1",
                    prizeType = PrizeType.BEER,
                    expiresAt = Instant.now().plusSeconds(86400),
                    status = BetStatus.FINISHED,
                    proposedWinnerUserId = marina,
                    athleteAConfirmed = true,
                    athleteBConfirmed = true,
                    confirmedWinnerUserId = marina,
                    athleteAResult = "5:30",
                    athleteBResult = "6:10"
                )
            )

            betService.seedBet(
                Bet(
                    createdByUserId = bruno,
                    athleteAUserId = felipe,
                    athleteBUserId = camila,
                    wodTitle = "Chipper Night",
                    prizeType = PrizeType.WATER,
                    expiresAt = Instant.now().plusSeconds(86400),
                    status = BetStatus.CANCELED
                )
            )

            betService.seedBet(
                Bet(
                    createdByUserId = marina,
                    athleteAUserId = ricardo,
                    athleteBUserId = bruno,
                    wodTitle = "Clean Ladder",
                    prizeType = PrizeType.SHAKE,
                    expiresAt = Instant.now().plusSeconds(86400),
                    status = BetStatus.DISPUTED,
                    proposedWinnerUserId = ricardo
                )
            )

            betService.seedBet(
                Bet(
                    createdByUserId = camila,
                    athleteAUserId = ricardo,
                    athleteBUserId = felipe,
                    wodTitle = "Expired Example",
                    prizeType = PrizeType.OTHER,
                    prizeOtherDescription = "Camiseta",
                    expiresAt = Instant.now().minusSeconds(3600),
                    status = BetStatus.OPEN
                )
            )
        }
    }
}
