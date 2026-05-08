package br.pucpr.authserver.events

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EventRepository : JpaRepository<Event, Long> {
    @Query(
        """
        SELECT e FROM Event e
        WHERE (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:status IS NULL OR e.status = :status)
          AND (:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%')))
          AND (:startDate IS NULL OR e.eventDate >= :startDate)
          AND (:endDate IS NULL OR e.eventDate <= :endDate)
        """
    )
    fun findWithFilters(
        @Param("name") name: String?,
        @Param("status") status: EventStatus?,
        @Param("location") location: String?,
        @Param("startDate") startDate: LocalDateTime?,
        @Param("endDate") endDate: LocalDateTime?,
        sort: Sort
    ): List<Event>
}

