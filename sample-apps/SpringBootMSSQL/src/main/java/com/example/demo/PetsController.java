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
    public record Rows(Integer rows, String comment) {}
    @PostMapping(path = "/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Rows create(@RequestBody PetCreate pet_data) {
        try {
            Integer rowsCreated = DatabaseHelper.createPetByName(pet_data.name);
            return new Rows(rowsCreated, null);
        } catch (Throwable e) {
            return new Rows(0, e.toString());
        }
    }
}