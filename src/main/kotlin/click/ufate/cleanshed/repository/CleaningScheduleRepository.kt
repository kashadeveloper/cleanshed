package click.ufate.cleanshed.repository

import click.ufate.cleanshed.entity.CleaningSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.DayOfWeek
import java.time.LocalDate

@Repository
interface CleaningScheduleRepository : JpaRepository<CleaningSchedule, Long> {
    fun findByDayOfWeekAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        dayOfWeek: DayOfWeek,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CleaningSchedule>

    @Query("""
        SELECT cs FROM CleaningSchedule cs 
        WHERE cs.person.id = :personId 
        AND cs.startDate <= :date 
        AND cs.endDate >= :date
    """)
    fun findByPersonIdAndDate(personId: Long, date: LocalDate): List<CleaningSchedule>

    @Query("""
        SELECT cs FROM CleaningSchedule cs 
        WHERE cs.place.id = :placeId 
        AND cs.startDate <= :date 
        AND cs.endDate >= :date
    """)
    fun findByPlaceIdAndDate(placeId: Long, date: LocalDate): List<CleaningSchedule>

    @Query("""
        SELECT cs FROM CleaningSchedule cs 
        WHERE cs.startDate <= :endDate 
        AND cs.endDate >= :startDate
        ORDER BY cs.startDate, cs.dayOfWeek
    """)
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<CleaningSchedule>

    fun findAllByOrderByStartDateAsc(): List<CleaningSchedule>
}
