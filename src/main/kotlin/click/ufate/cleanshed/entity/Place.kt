package click.ufate.cleanshed.entity

import jakarta.persistence.*

@Entity
@Table(name = "places")
class Place(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var location: String = "",

    @Column(nullable = false)
    var isCommon: Boolean = true,

    @ManyToMany(mappedBy = "allowedPlaces", fetch = FetchType.LAZY)
    var allowedPersons: MutableSet<Person> = mutableSetOf(),

    @Column(nullable = false)
    var active: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Place) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
