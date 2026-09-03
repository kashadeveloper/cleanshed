package click.ufate.cleanshed.controller

import click.ufate.cleanshed.dto.*
import click.ufate.cleanshed.entity.Place
import click.ufate.cleanshed.service.PlaceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/places")
class PlaceController(
    private val placeService: PlaceService
) {
    @GetMapping
    fun getAllPlaces(): ResponseEntity<List<PlaceDto>> {
        val places = placeService.getAllActivePlaces()
        return ResponseEntity.ok(places.map { it.toDto() })
    }

    @GetMapping("/{id}")
    fun getPlaceById(@PathVariable id: Long): ResponseEntity<PlaceDto> {
        val place = placeService.getPlaceById(id)
        return ResponseEntity.ok(place.toDto())
    }

    @GetMapping("/common")
    fun getCommonPlaces(): ResponseEntity<List<PlaceDto>> {
        val places = placeService.getCommonPlaces()
        return ResponseEntity.ok(places.map { it.toDto() })
    }

    @GetMapping("/search")
    fun searchPlaces(@RequestParam name: String): ResponseEntity<List<PlaceDto>> {
        val places = placeService.searchPlaces(name)
        return ResponseEntity.ok(places.map { it.toDto() })
    }

    @PostMapping
    fun createPlace(@RequestBody request: CreatePlaceRequest): ResponseEntity<PlaceDto> {
        val place = placeService.createPlace(request.name, request.location, request.isCommon)
        return ResponseEntity.ok(place.toDto())
    }

    @PutMapping("/{id}")
    fun updatePlace(
        @PathVariable id: Long,
        @RequestBody request: UpdatePlaceRequest
    ): ResponseEntity<PlaceDto> {
        val place = placeService.updatePlace(id, request.name, request.location, request.isCommon)
        return ResponseEntity.ok(place.toDto())
    }

    @DeleteMapping("/{id}")
    fun deletePlace(@PathVariable id: Long): ResponseEntity<Void> {
        placeService.deletePlace(id)
        return ResponseEntity.ok().build()
    }

    private fun Place.toDto() = PlaceDto(
        id = id,
        name = name,
        location = location,
        isCommon = isCommon,
        active = active
    )
}
