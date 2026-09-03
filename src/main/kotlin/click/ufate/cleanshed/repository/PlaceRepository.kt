package click.ufate.cleanshed.repository

import click.ufate.cleanshed.entity.Place
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlaceRepository : JpaRepository<Place, Long> {
    fun findByActiveTrue(): List<Place>
    fun findByIsCommonTrueAndActiveTrue(): List<Place>
    fun findByNameContainingIgnoreCase(name: String): List<Place>
}
