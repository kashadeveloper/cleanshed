package click.ufate.cleanshed.entity

import jakarta.persistence.*

@Entity
@Table(name = "persons")
class Person(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var room: String = "",

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "person_allowed_places",
        joinColumns = [JoinColumn(name = "person_id")],
        inverseJoinColumns = [JoinColumn(name = "place_id")]
    )
    var allowedPlaces: MutableSet<Place> = mutableSetOf(),

    @Column(nullable = false)
    var active: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Person) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
