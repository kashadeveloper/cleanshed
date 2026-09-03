package click.ufate.cleanshed.repository

import click.ufate.cleanshed.entity.CleaningStats
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface CleaningStatsRepository : JpaRepository<CleaningStats, Long> {
    @Query("""
        SELECT cs FROM CleaningStats cs 
        WHERE cs.place.id = :placeId 
        AND cs.cleaningDate BETWEEN :startDate AND :endDate
    """)
    fun findByPlaceIdAndDateRange(placeId: Long, startDate: LocalDate, endDate: LocalDate): List<CleaningStats>

    @Query("""
        SELECT cs FROM CleaningStats cs 
        WHERE cs.person.id = :personId 
        AND cs.cleaningDate BETWEEN :startDate AND :endDate
    """)
    fun findByPersonIdAndDateRange(personId: Long, startDate: LocalDate, endDate: LocalDate): List<CleaningStats>

    @Query("""
        SELECT cs.place.id, COUNT(cs) 
        FROM CleaningStats cs 
        WHERE cs.cleaningDate BETWEEN :startDate AND :endDate
        GROUP BY cs.place.id
    """)
    fun countByPlaceAndDateRange(startDate: LocalDate, endDate: LocalDate): List<Array<Any>>

    @Query("""
        SELECT cs.person.id, cs.place.id, COUNT(cs) 
        FROM CleaningStats cs 
        WHERE cs.cleaningDate BETWEEN :startDate AND :endDate
        GROUP BY cs.person.id, cs.place.id
    """)
    fun countByPersonAndPlaceAndDateRange(startDate: LocalDate, endDate: LocalDate): List<Array<Any>>

    @Query("""
        SELECT cs FROM CleaningStats cs 
        WHERE cs.cleaningDate BETWEEN :startDate AND :endDate
    """)
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<CleaningStats>

    fun deleteByScheduleId(scheduleId: Long)
}
