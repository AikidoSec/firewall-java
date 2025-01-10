package dev.aikido.SpringWebfluxSampleApp;
import dev.aikido.SpringWebfluxSampleApp.models.Pet;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pets") // Base URL for all routes in this controller
public class PetsController {

    @GetMapping("/")
    public Flux<Object> index() {
        return Flux.fromIterable(DatabaseHelper.getAllPets());
    }

    @GetMapping("/{id}")
    public Mono<Pet> pet(@PathVariable("id") Integer id) {
        return Mono.justOrEmpty(DatabaseHelper.getPetById(id));
    }

    private record PetCreate(String name) {}
    public record Rows(Integer rows) {}

    @PostMapping(path = "/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Rows> create(@RequestBody PetCreate pet_data) {
        return Mono.fromCallable(() -> {
            Integer rowsCreated = DatabaseHelper.createPetByName(pet_data.name);
            return new Rows(rowsCreated);
        });
    }
}
