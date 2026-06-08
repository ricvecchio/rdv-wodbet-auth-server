package br.pucpr.authserver.bets.controllers

import br.pucpr.authserver.bets.dtos.requests.CreateBetRequest
import br.pucpr.authserver.bets.dtos.requests.ProposeWinnerRequest
import br.pucpr.authserver.bets.dtos.requests.UpdateBetResultRequest
import br.pucpr.authserver.bets.dtos.requests.VoteBetRequest
import br.pucpr.authserver.bets.dtos.responses.BetResponse
import br.pucpr.authserver.bets.services.BetService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/bets")
@Tag(name = "Bets", description = "Gerenciamento de apostas")
class BetController(private val service: BetService) {

    @GetMapping
    @Operation(summary = "Listar apostas", description = "Lista todas as apostas ordenadas por createdAt DESC.")
    fun list(): ResponseEntity<List<BetResponse>> = ResponseEntity.ok(service.findAll())

    @GetMapping("/{id}")
    @Operation(summary = "Buscar aposta por ID")
    fun getById(@PathVariable id: Long): ResponseEntity<BetResponse> = ResponseEntity.ok(service.findById(id))

    @PostMapping
    @Operation(summary = "Criar aposta")
    fun create(@Valid @RequestBody request: CreateBetRequest): ResponseEntity<BetResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}/winner")
    @Operation(summary = "Propor vencedor")
    fun proposeWinner(@PathVariable id: Long, @Valid @RequestBody request: ProposeWinnerRequest): ResponseEntity<BetResponse> =
        ResponseEntity.ok(service.proposeWinner(id, request))

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirmar vencedor")
    fun confirm(@PathVariable id: Long, @RequestBody request: Map<String, String>): ResponseEntity<BetResponse> =
        ResponseEntity.ok(service.confirmWinner(id, request["confirmerUserId"] ?: ""))

    @PutMapping("/{id}/reject")
    @Operation(summary = "Rejeitar vencedor")
    fun reject(@PathVariable id: Long, @RequestBody request: Map<String, String>): ResponseEntity<BetResponse> =
        ResponseEntity.ok(service.rejectWinner(id, request["rejectorUserId"] ?: ""))

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancelar aposta")
    fun cancel(@PathVariable id: Long, @RequestBody request: Map<String, String>): ResponseEntity<BetResponse> =
        ResponseEntity.ok(service.cancel(id, request["requesterUserId"] ?: ""))

    @PutMapping("/{id}/result")
    @Operation(summary = "Atualizar resultado")
    fun result(@PathVariable id: Long, @Valid @RequestBody request: UpdateBetResultRequest): ResponseEntity<BetResponse> =
        ResponseEntity.ok(service.updateResult(id, request))

    @PutMapping("/{id}/vote")
    @Operation(summary = "Votar em atleta")
    fun vote(@PathVariable id: Long, @Valid @RequestBody request: VoteBetRequest): ResponseEntity<BetResponse> =
        ResponseEntity.ok(service.vote(id, request))
}

