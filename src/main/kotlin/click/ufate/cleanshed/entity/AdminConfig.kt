package click.ufate.cleanshed.entity

import jakarta.persistence.*

@Entity
@Table(name = "admin_config")
class AdminConfig(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 1,

    @Column(nullable = false)
    var masterPassword: String = "",

    @Column(nullable = false)
    var passwordGenerated: Boolean = false,

    @Column(nullable = true)
    var sessionToken: String? = null,

    @Column(nullable = true)
    var sessionExpiry: java.time.LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AdminConfig) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
