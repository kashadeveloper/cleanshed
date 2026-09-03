package click.ufate.cleanshed.service

import click.ufate.cleanshed.entity.Place
import click.ufate.cleanshed.repository.PlaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceService(
    private val placeRepository: PlaceRepository
) {
    fun getAllActivePlaces(): List<Place> = placeRepository.findByActiveTrue()

    fun getAllPlaces(): List<Place> = placeRepository.findAll()

    fun getPlaceById(id: Long): Place = placeRepository.findById(id)
        .orElseThrow { IllegalArgumentException("Place not found with id: $id") }

    fun getCommonPlaces(): List<Place> = placeRepository.findByIsCommonTrueAndActiveTrue()

    fun searchPlaces(name: String): List<Place> = placeRepository.findByNameContainingIgnoreCase(name)

    @Transactional
    fun createPlace(name: String, location: String, isCommon: Boolean = true): Place {
        val place = Place(
            name = name.trim(),
            location = location.trim(),
            isCommon = isCommon,
            active = true
        )
        return placeRepository.save(place)
    }

    @Transactional
    fun updatePlace(id: Long, name: String, location: String, isCommon: Boolean): Place {
        val place = getPlaceById(id)
        place.name = name.trim()
        place.location = location.trim()
        place.isCommon = isCommon
        return placeRepository.save(place)
    }

    @Transactional
    fun deletePlace(id: Long) {
        val place = getPlaceById(id)
        place.active = false
        placeRepository.save(place)
    }
}
