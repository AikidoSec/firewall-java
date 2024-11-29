package com.example.demo;

import com.example.demo.models.Pet;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/pets") // Base URL for all routes in this controller
public class PetsController {
    @GetMapping("/")
    public ArrayList<Object> index() {
        return DatabaseHelper.getAllPets();
    }

    @GetMapping("/{id}")
    public Pet pet(@PathVariable("id") Integer id) {
        return DatabaseHelper.getPetById(id);
    }

    private record PetCreate(String name) {}
    public record Rows(Integer rows) {}
    @PostMapping(path = "/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Rows create(@RequestBody PetCreate pet_data) {
        Integer rowsCreated = DatabaseHelper.createPetByName(pet_data.name, "mysql");
        return new Rows(rowsCreated);
    }

    @PostMapping(path = "/create/mariadb",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Rows createmariadb(@RequestBody PetCreate pet_data) {
        Integer rowsCreated = DatabaseHelper.createPetByName(pet_data.name, "mariadb");
        return new Rows(rowsCreated);
    }
}