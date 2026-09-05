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

    fun getAllSchedules(): List<CleaningSchedule> {
        return scheduleRepository.findAllByOrderByStartDateAsc()
    }

    @Transactional
    fun generateScheduleForDay(dayOfWeek: DayOfWeek, weeks: Int = 4): List<CleaningSchedule> {
        val persons = personService.getAllActivePersons().shuffled()
        val places = placeService.getAllActivePlaces()

        if (persons.isEmpty() || places.isEmpty()) {
            throw IllegalStateException("No active persons or common places found")
        }

        val startDate = LocalDate.now()

        scheduleRepository.deleteAll()

        val weeklyAssignments = calculateRotationAssignments(persons, places, weeks)

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

    /**
     * Simple, fair rotation without relying on database history.
     *
     * Rules:
     * - All spots are included each day; personal spots come first and are assigned only to their owners;
     * - A person cleans only one spot per day—either personal or communal (Phase 2 involves
     *   only those without an assignment; duplicate assignments occur only when there are more
     *   spots than people, in which case the second spot goes to someone who doesn't have a personal spot that day);
     * - No one cleans the same spot they cleaned the previous week (provided an alternative exists);
     * - Workload is balanced using counters within the current generation cycle.
     */
    private fun calculateRotationAssignments(
        persons: List<Person>,
        places: List<Place>,
        weeks: Int
    ): List<List<Pair<Person, Place>>> {
        val weeklyAssignments = mutableListOf<List<Pair<Person, Place>>>()

        val orderedPersonal = places.filter { !it.isCommon }.shuffled()
        val orderedCommon = places.filter { it.isCommon }.shuffled()

        val pairCount = mutableMapOf<Pair<Long, Long>, Int>()
        val totalCount = mutableMapOf<Long, Int>()
        var lastWeekPlaces = mutableMapOf<Long, MutableSet<Long>>()

        for (week in 0 until weeks) {
            val weeklyLoad = mutableMapOf<Long, Int>()
            val weekAssignments = mutableListOf<Pair<Person, Place>>()
            val thisWeekPlaces = mutableMapOf<Long, MutableSet<Long>>()
            val busyToday = mutableSetOf<Long>()
            val hasPersonalToday = mutableSetOf<Long>()

            fun record(person: Person, place: Place) {
                weekAssignments.add(person to place)
                busyToday.add(person.id)
                if (!place.isCommon) hasPersonalToday.add(person.id)
                weeklyLoad[person.id] = (weeklyLoad[person.id] ?: 0) + 1
                totalCount[person.id] = (totalCount[person.id] ?: 0) + 1
                pairCount[person.id to place.id] =
                    (pairCount[person.id to place.id] ?: 0) + 1
                thisWeekPlaces.getOrPut(person.id) { mutableSetOf() }.add(place.id)
            }

            fun repeatPenalty(person: Person, place: Place): Int =
                if (lastWeekPlaces[person.id]?.contains(place.id) == true) 1000 else 0

            for (place in orderedPersonal) {
                val owners = persons.filter { person ->
                    person.allowedPlaces.any { it.id == place.id }
                }
                val candidates = if (owners.isEmpty()) persons else owners
                if (candidates.isEmpty()) continue

                fun score(person: Person): Int {
                    val pair = pairCount[person.id to place.id] ?: 0
                    val total = totalCount[person.id] ?: 0
                    val busy = if (person.id in busyToday) 500 else 0
                    return repeatPenalty(person, place) + busy + pair * 100 + total * 10
                }

                val minScore = candidates.minOf { score(it) }
                record(candidates.filter { score(it) == minScore }.random(), place)
            }

            for (place in orderedCommon) {
                val free = persons.filter { it.id !in busyToday }
                val pool = if (free.isNotEmpty()) {
                    free
                } else {
                    val withoutPersonal =
                        persons.filter { it.id in busyToday && it.id !in hasPersonalToday }
                    if (withoutPersonal.isNotEmpty()) withoutPersonal else persons
                }

                fun score(person: Person): Int {
                    val pair = pairCount[person.id to place.id] ?: 0
                    val total = totalCount[person.id] ?: 0
                    val dayLoad = weeklyLoad[person.id] ?: 0
                    return repeatPenalty(person, place) + pair * 100 + total * 10 + dayLoad * 5
                }

                val minScore = pool.minOf { score(it) }
                record(pool.filter { score(it) == minScore }.random(), place)
            }

            weeklyAssignments.add(weekAssignments)
            lastWeekPlaces = thisWeekPlaces
        }

        return weeklyAssignments
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
        val allPlaces = placeService.getAllActivePlaces()

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
