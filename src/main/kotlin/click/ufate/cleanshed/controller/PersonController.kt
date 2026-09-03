package click.ufate.cleanshed.controller

import click.ufate.cleanshed.dto.*
import click.ufate.cleanshed.entity.Person
import click.ufate.cleanshed.service.PersonService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/persons")
class PersonController(
    private val personService: PersonService
) {
    @GetMapping
    fun getAllPersons(): ResponseEntity<List<PersonDto>> {
        val persons = personService.getAllActivePersons()
        return ResponseEntity.ok(persons.map { it.toDto() })
    }

    @GetMapping("/{id}")
    fun getPersonById(@PathVariable id: Long): ResponseEntity<PersonDto> {
        val person = personService.getPersonById(id)
        return ResponseEntity.ok(person.toDto())
    }

    @GetMapping("/search")
    fun searchPersons(@RequestParam name: String): ResponseEntity<List<PersonDto>> {
        val persons = personService.searchPersons(name)
        return ResponseEntity.ok(persons.map { it.toDto() })
    }

    @PostMapping
    fun createPerson(@RequestBody request: CreatePersonRequest): ResponseEntity<PersonDto> {
        val person = personService.createPerson(request.name, request.room)
        return ResponseEntity.ok(person.toDto())
    }

    @PutMapping("/{id}")
    fun updatePerson(
        @PathVariable id: Long,
        @RequestBody request: UpdatePersonRequest
    ): ResponseEntity<PersonDto> {
        val person = personService.updatePerson(id, request.name, request.room)
        return ResponseEntity.ok(person.toDto())
    }

    @DeleteMapping("/{id}")
    fun deletePerson(@PathVariable id: Long): ResponseEntity<Void> {
        personService.deletePerson(id)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{personId}/allowed-places")
    fun addAllowedPlace(
        @PathVariable personId: Long,
        @RequestBody request: AllowPlaceRequest
    ): ResponseEntity<PersonDto> {
        val person = personService.addAllowedPlace(personId, request.placeId)
        return ResponseEntity.ok(person.toDto())
    }

    @DeleteMapping("/{personId}/allowed-places/{placeId}")
    fun removeAllowedPlace(
        @PathVariable personId: Long,
        @PathVariable placeId: Long
    ): ResponseEntity<PersonDto> {
        val person = personService.removeAllowedPlace(personId, placeId)
        return ResponseEntity.ok(person.toDto())
    }

    @GetMapping("/{personId}/allowed-places")
    fun getPersonAllowedPlaces(@PathVariable personId: Long): ResponseEntity<List<Long>> {
        val places = personService.getPersonAllowedPlaces(personId)
        return ResponseEntity.ok(places.map { it.id })
    }

    private fun Person.toDto() = PersonDto(
        id = id,
        name = name,
        room = room,
        active = active,
        allowedPlaceIds = allowedPlaces.map { it.id }
    )
}
