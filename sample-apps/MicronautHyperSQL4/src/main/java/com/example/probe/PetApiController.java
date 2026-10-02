package com.example.probe;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;

/** Standard e2e endpoint: POST /api/pets/create with a deliberately vulnerable INSERT (HyperSQL). */
@Controller("/api/pets")
public class PetApiController {

    @Post("/create")
    public Map<String, Object> create(@Body Map<String, Object> body) throws Exception {
        String petName = String.valueOf(body.getOrDefault("name", ""));
        try (Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:petsdb", "SA", "")) {
            Statement st = c.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS pets (pet_name VARCHAR(255), owner VARCHAR(255))");
            st.executeUpdate("INSERT INTO pets (pet_name, owner) VALUES ('" + petName + "', 'Aikido Security')");
            return Map.of("created", petName);
        }
    }
}
