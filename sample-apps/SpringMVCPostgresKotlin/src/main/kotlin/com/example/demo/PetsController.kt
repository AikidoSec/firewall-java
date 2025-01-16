package com.example.demo

import com.example.demo.models.Pet
import com.example.demo.utils.DatabaseHelper
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/pets") // Base URL for all routes in this controller
class PetsController {

    @GetMapping("/")
    fun index(): List<Pet> {
        return DatabaseHelper.getAllPets()
    }

    @GetMapping("/{id}")
    fun pet(@PathVariable("id") id: Int): Pet {
        return DatabaseHelper.getPetById(id)
    }

    data class PetCreate(val name: String)

    data class Rows(val rows: Int)

    @PostMapping(
        path = ["/create"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun create(@RequestBody petData: PetCreate): Rows {
        val rowsCreated = DatabaseHelper.createPetByName(petData.name)
        return Rows(rowsCreated)
    }
}