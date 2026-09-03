package click.ufate.cleanshed.entity

import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalDate

@Entity
@Table(name = "cleaning_schedule")
class CleaningSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    var person: Person? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    var place: Place? = null,

    @Column(nullable = false)
    var dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,

    @Column(nullable = false)
    var startDate: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    var endDate: LocalDate = LocalDate.now().plusWeeks(4),

    @Column(nullable = false)
    var completed: Boolean = false,

    @Column(nullable = true)
    var completedDate: LocalDate? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CleaningSchedule) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
