package click.ufate.cleanshed.controller

import click.ufate.cleanshed.dto.*
import click.ufate.cleanshed.entity.CleaningSchedule
import click.ufate.cleanshed.service.ScheduleService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.DayOfWeek
import java.time.LocalDate

@RestController
@RequestMapping("/api/schedule")
class ScheduleController(
    private val scheduleService: ScheduleService
) {
    @GetMapping("/today")
    fun getTodaySchedule(): ResponseEntity<TodayScheduleDto> {
        val today = LocalDate.now()
        val schedules = scheduleService.getScheduleForDate(today)

        val assignments = schedules.map { schedule ->
            ScheduleAssignmentDto(
                personId = schedule.person?.id ?: 0,
                personName = schedule.person?.name ?: "",
                placeId = schedule.place?.id ?: 0,
                placeName = schedule.place?.name ?: "",
                placeLocation = schedule.place?.location ?: "",
                completed = schedule.completed,
                scheduleId = schedule.id
            )
        }

        return ResponseEntity.ok(TodayScheduleDto(
            date = today.toString(),
            dayOfWeek = today.dayOfWeek.name,
            assignments = assignments
        ))
    }

    @GetMapping("/week/{date}")
    fun getWeekSchedule(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<List<ScheduleDto>> {
        val schedules = scheduleService.getScheduleForWeek(date)
        return ResponseEntity.ok(schedules.map { it.toDto() })
    }

    @GetMapping("/all")
    fun getAllSchedules(): ResponseEntity<List<ScheduleDto>> {
        val schedules = scheduleService.getAllSchedules()
        return ResponseEntity.ok(schedules.map { it.toDto() })
    }

    @PostMapping("/generate")
    fun generateSchedule(@RequestBody request: GenerateScheduleRequest): ResponseEntity<Map<String, Int>> {
        val dayOfWeek = DayOfWeek.valueOf(request.dayOfWeek.uppercase())
        val count = scheduleService.generateScheduleForDay(dayOfWeek, request.weeks)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @PutMapping("/{id}/complete")
    fun markAsCompleted(@PathVariable id: Long): ResponseEntity<ScheduleDto> {
        val schedule = scheduleService.markScheduleAsCompleted(id)
        return ResponseEntity.ok(schedule.toDto())
    }

    @PutMapping("/{id}/incomplete")
    fun markAsIncomplete(@PathVariable id: Long): ResponseEntity<ScheduleDto> {
        val schedule = scheduleService.markScheduleAsIncomplete(id)
        return ResponseEntity.ok(schedule.toDto())
    }

    @GetMapping("/stats")
    fun getStats(
        @RequestParam(required = false) personId: Long?,
        @RequestParam(required = false) placeId: Long?
    ): ResponseEntity<Any> {
        val stats = scheduleService.getCleaningStats(personId, placeId)
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/stats/summary")
    fun getStatsSummary(): ResponseEntity<StatsSummaryDto> {
        val summary = scheduleService.getStatsSummary()
        @Suppress("UNCHECKED_CAST")
        return ResponseEntity.ok(StatsSummaryDto(
            persons = (summary["persons"] as List<click.ufate.cleanshed.entity.Person>).map {
                click.ufate.cleanshed.dto.PersonDto(
                    id = it.id,
                    name = it.name,
                    room = it.room,
                    active = it.active,
                    allowedPlaceIds = it.allowedPlaces.map { p -> p.id }
                )
            },
            places = (summary["places"] as List<click.ufate.cleanshed.entity.Place>).map {
                click.ufate.cleanshed.dto.PlaceDto(
                    id = it.id,
                    name = it.name,
                    location = it.location,
                    isCommon = it.isCommon,
                    active = it.active
                )
            },
            stats = summary["stats"] as Map<Long, Map<Long, Int>>,
            dateRange = (summary["dateRange"] as Map<String, LocalDate>).let { dr ->
                mapOf("start" to dr["start"]!!.toString(), "end" to dr["end"]!!.toString())
            }
        ))
    }

    private fun CleaningSchedule.toDto() = ScheduleDto(
        id = id,
        personId = person?.id ?: 0,
        personName = person?.name ?: "",
        placeId = place?.id ?: 0,
        placeName = place?.name ?: "",
        placeLocation = place?.location ?: "",
        dayOfWeek = dayOfWeek.name,
        startDate = startDate.toString(),
        endDate = endDate.toString(),
        completed = completed,
        completedDate = completedDate?.toString()
    )
}
