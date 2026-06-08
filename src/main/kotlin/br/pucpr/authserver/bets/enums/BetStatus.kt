package br.pucpr.authserver.bets.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class BetStatus(@JsonValue val jsonValue: String) {
    OPEN("open"),
    FINISHED("finished"),
    CANCELED("canceled"),
    DISPUTED("disputed"),
    EXPIRED("expired");

    companion object {
        @JsonCreator
        @JvmStatic
        fun from(value: String): BetStatus = entries.firstOrNull { it.jsonValue == value.lowercase() }
            ?: throw IllegalArgumentException("Invalid bet status: $value")
    }
}

