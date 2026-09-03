package click.ufate.cleanshed.dto

data class PersonDto(
    val id: Long,
    val name: String,
    val room: String,
    val active: Boolean,
    val allowedPlaceIds: List<Long>
)

data class CreatePersonRequest(
    val name: String,
    val room: String
)

data class UpdatePersonRequest(
    val name: String,
    val room: String
)

data class PlaceDto(
    val id: Long,
    val name: String,
    val location: String,
    val isCommon: Boolean,
    val active: Boolean
)

data class CreatePlaceRequest(
    val name: String,
    val location: String,
    val isCommon: Boolean = true
)

data class UpdatePlaceRequest(
    val name: String,
    val location: String,
    val isCommon: Boolean
)

data class ScheduleDto(
    val id: Long,
    val personId: Long,
    val personName: String,
    val placeId: Long,
    val placeName: String,
    val placeLocation: String,
    val dayOfWeek: String,
    val startDate: String,
    val endDate: String,
    val completed: Boolean,
    val completedDate: String?
)

data class GenerateScheduleRequest(
    val dayOfWeek: String,
    val weeks: Int = 4
)

data class AdminLoginRequest(
    val password: String
)

data class AdminLoginResponse(
    val token: String,
    val expiresIn: Int = 86400
)

data class AllowPlaceRequest(
    val placeId: Long
)

data class TodayScheduleDto(
    val date: String,
    val dayOfWeek: String,
    val assignments: List<ScheduleAssignmentDto>
)

data class ScheduleAssignmentDto(
    val personId: Long,
    val personName: String,
    val placeId: Long,
    val placeName: String,
    val placeLocation: String,
    val completed: Boolean,
    val scheduleId: Long
)

data class StatsSummaryDto(
    val persons: List<PersonDto>,
    val places: List<PlaceDto>,
    val stats: Map<Long, Map<Long, Int>>,
    val dateRange: Map<String, String>
)

data class PasswordResetResponse(
    val newPassword: String
)
