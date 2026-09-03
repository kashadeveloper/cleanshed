package click.ufate.cleanshed.service

import click.ufate.cleanshed.entity.Person
import click.ufate.cleanshed.entity.Place
import click.ufate.cleanshed.repository.PersonRepository
import click.ufate.cleanshed.repository.PlaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PersonService(
    private val personRepository: PersonRepository,
    private val placeRepository: PlaceRepository
) {
    fun getAllActivePersons(): List<Person> = personRepository.findByActiveTrue()

    fun getAllPersons(): List<Person> = personRepository.findAll()

    fun getPersonById(id: Long): Person = personRepository.findById(id)
        .orElseThrow { IllegalArgumentException("Person not found with id: $id") }

    fun searchPersons(name: String): List<Person> = personRepository.findByNameContainingIgnoreCase(name)

    @Transactional
    fun createPerson(name: String, room: String): Person {
        val person = Person(
            name = name.trim(),
            room = room.trim(),
            active = true
        )
        return personRepository.save(person)
    }

    @Transactional
    fun updatePerson(id: Long, name: String, room: String): Person {
        val person = getPersonById(id)
        person.name = name.trim()
        person.room = room.trim()
        return personRepository.save(person)
    }

    @Transactional
    fun deletePerson(id: Long) {
        val person = getPersonById(id)
        person.active = false
        personRepository.save(person)
    }

    @Transactional
    fun addAllowedPlace(personId: Long, placeId: Long): Person {
        val person = getPersonById(personId)
        val place = placeRepository.findById(placeId)
            .orElseThrow { IllegalArgumentException("Place not found with id: $placeId") }
        person.allowedPlaces.add(place)
        return personRepository.save(person)
    }

    @Transactional
    fun removeAllowedPlace(personId: Long, placeId: Long): Person {
        val person = getPersonById(personId)
        person.allowedPlaces.removeIf { it.id == placeId }
        return personRepository.save(person)
    }

    fun getPersonAllowedPlaces(personId: Long): Set<Place> {
        val person = getPersonById(personId)
        return person.allowedPlaces
    }
}
