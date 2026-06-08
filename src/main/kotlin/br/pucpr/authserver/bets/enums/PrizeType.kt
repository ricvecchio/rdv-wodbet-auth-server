package br.pucpr.authserver.bets.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class PrizeType(@JsonValue val jsonValue: String) {
    WATER("water"),
    GATORADE("gatorade"),
    BEER("beer"),
    SHAKE("shake"),
    OTHER("other");

    companion object {
        @JsonCreator
        @JvmStatic
        fun from(value: String): PrizeType = entries.firstOrNull { it.jsonValue == value.lowercase() }
            ?: throw IllegalArgumentException("Invalid prizeType: $value")
    }
}

