package click.ufate.cleanshed.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "cleaning_stats")
class CleaningStats(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    var person: Person? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    var place: Place? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    var schedule: CleaningSchedule? = null,

    @Column(nullable = false)
    var cleaningDate: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    var completed: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CleaningStats) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
