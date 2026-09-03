package click.ufate.cleanshed.repository

import click.ufate.cleanshed.entity.Person
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonRepository : JpaRepository<Person, Long> {
    @EntityGraph(attributePaths = ["allowedPlaces"])
    fun findByActiveTrue(): List<Person>
    fun findByNameContainingIgnoreCase(name: String): List<Person>
}
