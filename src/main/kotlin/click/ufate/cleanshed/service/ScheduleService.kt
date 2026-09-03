package click.ufate.cleanshed.service

import click.ufate.cleanshed.entity.CleaningSchedule
import click.ufate.cleanshed.entity.CleaningStats
import click.ufate.cleanshed.entity.Person
import click.ufate.cleanshed.entity.Place
import click.ufate.cleanshed.repository.CleaningScheduleRepository
import click.ufate.cleanshed.repository.CleaningStatsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate

@Service
class ScheduleService(
    private val scheduleRepository: CleaningScheduleRepository,
    private val statsRepository: CleaningStatsRepository,
    private val personService: PersonService,
    private val placeService: PlaceService
) {
    fun getScheduleForDate(date: LocalDate): List<CleaningSchedule> {
        val dayOfWeek = date.dayOfWeek
        return scheduleRepository.findByDayOfWeekAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            dayOfWeek = dayOfWeek,
            startDate = date,
            endDate = date
        )
    }

    fun getScheduleForWeek(weekStart: LocalDate): List<CleaningSchedule> {
        val weekEnd = weekStart.plusDays(6)
        return scheduleRepository.findByDateRange(weekStart, weekEnd)
    }

    fun getScheduleForDateRange(startDate: LocalDate, endDate: LocalDate): List<CleaningSchedule> {
        return scheduleRepository.findByDateRange(startDate, endDate)
    }

    @Transactional
    fun generateScheduleForDay(dayOfWeek: DayOfWeek, weeks: Int = 4): List<CleaningSchedule> {
        val persons = personService.getAllActivePersons()
        val places = placeService.getAllPlaces()

        if (persons.isEmpty() || places.isEmpty()) {
            throw IllegalStateException("No active persons or common places found")
        }

        val startDate = LocalDate.now()
        val endDate = startDate.plusWeeks(weeks.toLong())

        val existingSchedules = scheduleRepository.findByDateRange(startDate, endDate)
            .filter { it.dayOfWeek == dayOfWeek }

        scheduleRepository.deleteAll(existingSchedules)

        val stats = calculateStatsForDateRange(startDate, endDate)
        val weeklyAssignments = calculateFairAssignments(persons, places, dayOfWeek, weeks, stats)

        val schedules = mutableListOf<CleaningSchedule>()

        for (week in 0 until weeks) {
            val currentDate = startDate.plusWeeks(week.toLong())
            val weekStart = currentDate.with(java.time.temporal.TemporalAdjusters.nextOrSame(dayOfWeek))

            for (assignment in weeklyAssignments[week]) {
                val schedule = CleaningSchedule(
                    person = assignment.first,
                    place = assignment.second,
                    dayOfWeek = dayOfWeek,
                    startDate = weekStart,
                    endDate = weekStart,
                    completed = false
                )
                schedules.add(scheduleRepository.save(schedule))
            }
        }

        return schedules
    }

    private fun calculateFairAssignments(
        persons: List<Person>,
        places: List<Place>,
        dayOfWeek: DayOfWeek,
        weeks: Int,
        stats: Map<Long, MutableMap<Long, Int>>
    ): List<List<Pair<Person, Place>>> {
        val weeklyAssignments = mutableListOf<List<Pair<Person, Place>>>()

        for (week in 0 until weeks) {
            val assignedPersonIds = mutableSetOf<Long>()
            val weekAssignments = mutableListOf<Pair<Person, Place>>()

            val sortedPlaces = places.sortedBy { place ->
                stats.values.sumOf { it[place.id] ?: 0 }
            }

            for (place in sortedPlaces) {
                val eligiblePersons = persons.filter { person ->
                    !assignedPersonIds.contains(person.id) &&
                            (place.isCommon || person.allowedPlaces.any { it.id == place.id })
                }

                if (eligiblePersons.isEmpty()) {
                    continue
                }

                val selectedPerson = eligiblePersons.minByOrNull { person ->
                    val personStats = stats[person.id] ?: mutableMapOf()
                    val placeCount = personStats[place.id] ?: 0
                    val totalCount = personStats.values.sum()
                    placeCount * 10 + totalCount
                } ?: eligiblePersons.random()

                weekAssignments.add(Pair(selectedPerson, place))
                assignedPersonIds.add(selectedPerson.id)

                stats[selectedPerson.id]?.let { personStats ->
                    personStats[place.id] = (personStats[place.id] ?: 0) + 1
                }
            }

            weeklyAssignments.add(weekAssignments)
        }

        return weeklyAssignments
    }

    private fun calculateStatsForDateRange(startDate: LocalDate, endDate: LocalDate): Map<Long, MutableMap<Long, Int>> {
        val stats = mutableMapOf<Long, MutableMap<Long, Int>>()
        val allPersons = personService.getAllActivePersons()
        val allPlaces = placeService.getAllPlaces()

        allPersons.forEach { person ->
            stats[person.id] = mutableMapOf()
            allPlaces.forEach { place ->
                stats[person.id]!![place.id] = 0
            }
        }

        val existingStats = statsRepository.findByDateRange(startDate, endDate)
        existingStats.forEach { stat ->
            val personId = stat.person?.id ?: return@forEach
            val placeId = stat.place?.id ?: return@forEach
            stats[personId]?.let { personStats ->
                personStats[placeId] = (personStats[placeId] ?: 0) + 1
            }
        }

        return stats
    }
    
    @Transactional
    fun markScheduleAsCompleted(scheduleId: Long): CleaningSchedule {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { IllegalArgumentException("Schedule not found with id: $scheduleId") }

        schedule.completed = true
        schedule.completedDate = LocalDate.now()

        val stat = CleaningStats(
            person = schedule.person,
            place = schedule.place,
            cleaningDate = schedule.completedDate!!,
            completed = true
        )
        statsRepository.save(stat)

        return scheduleRepository.save(schedule)
    }

    @Transactional
    fun markScheduleAsIncomplete(scheduleId: Long): CleaningSchedule {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { IllegalArgumentException("Schedule not found with id: $scheduleId") }

        statsRepository.deleteByScheduleId(scheduleId)

        schedule.completed = false
        schedule.completedDate = null

        return scheduleRepository.save(schedule)
    }

    fun getCleaningStats(personId: Long? = null, placeId: Long? = null): List<CleaningStats> {
        val startDate = LocalDate.now().minusMonths(3)
        val endDate = LocalDate.now()

        return when {
            personId != null && placeId != null -> {
                statsRepository.findByPersonIdAndDateRange(personId, startDate, endDate)
                    .filter { it.place?.id == placeId }
            }
            personId != null -> statsRepository.findByPersonIdAndDateRange(personId, startDate, endDate)
            placeId != null -> statsRepository.findByPlaceIdAndDateRange(placeId, startDate, endDate)
            else -> statsRepository.findAll()
        }
    }

    fun getStatsSummary(): Map<String, Any> {
        val startDate = LocalDate.now().minusMonths(3)
        val endDate = LocalDate.now()

        val allPersons = personService.getAllActivePersons()
        val allPlaces = placeService.getAllPlaces()

        val personStats = mutableMapOf<Long, MutableMap<Long, Int>>()
        allPersons.forEach { person ->
            personStats[person.id] = mutableMapOf()
            allPlaces.forEach { place ->
                personStats[person.id]!![place.id] = 0
            }
        }

        val stats = statsRepository.findByDateRange(startDate, endDate)
        stats.forEach { stat ->
            val personId = stat.person?.id ?: return@forEach
            val placeId = stat.place?.id ?: return@forEach
            personStats[personId]?.let { ps ->
                ps[placeId] = (ps[placeId] ?: 0) + 1
            }
        }

        return mapOf(
            "persons" to allPersons,
            "places" to allPlaces,
            "stats" to personStats,
            "dateRange" to mapOf("start" to startDate, "end" to endDate)
        )
    }
}
