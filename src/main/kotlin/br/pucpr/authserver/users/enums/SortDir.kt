package br.pucpr.authserver.users.enums

import br.pucpr.authserver.exceptions.BadRequestException

enum class SortDir {
    ASC, DESC;

    companion object {
        fun findOrNull(sortDir: String) = entries.find { it.name == sortDir.uppercase() }
        fun find(sortDir: String) = findOrNull(sortDir) ?: throw BadRequestException("Invalid sort dir")
    }
}
