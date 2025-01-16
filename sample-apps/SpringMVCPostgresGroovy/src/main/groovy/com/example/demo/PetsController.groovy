package com.example.demo

import com.example.demo.models.Pet
import com.example.demo.utils.DatabaseHelper
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/pets") // Base URL for all routes in this controller
class PetsController {

    @GetMapping("/")
    List<Pet> index() {
        return DatabaseHelper.getAllPets()
    }

    @GetMapping("/{id}")
    Pet pet(@PathVariable("id") Integer id) {
        return DatabaseHelper.getPetById(id)
    }

     static class PetCreate {
        String name
     }

    static class Rows {
        Integer rows

        Rows(Integer rows) {
            this.rows = rows
        }
    }

    @PostMapping(path = "/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    Rows create(@RequestBody PetCreate petData) {
        Integer rowsCreated = DatabaseHelper.createPetByName(petData.name)
        return new Rows(rowsCreated)
    }
}
