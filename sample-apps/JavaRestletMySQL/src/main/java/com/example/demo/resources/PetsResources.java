package com.example.demo.resources;

import com.example.demo.DatabaseHelper;
import com.example.demo.models.Pet;
import org.restlet.data.MediaType;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.util.ArrayList;

public class PetsResources extends ServerResource {

    @Get("json")
    public ArrayList<Object> index() {
        return DatabaseHelper.getAllPets();
    }

    @Get("json")
    public Pet pet() {
        Integer id = Integer.valueOf(getAttribute("id"));
        return DatabaseHelper.getPetById(id);
    }

    private static class PetCreate {
        public String name;

        // Default constructor is needed for JSON deserialization
        public PetCreate() {}

        public PetCreate(String name) {
            this.name = name;
        }
    }

    private static class Rows {
        public Integer rows;

        public Rows(Integer rows) {
            this.rows = rows;
        }
    }

    @Post("json")
    public Rows create(String jsonInput) {
        PetCreate petData = parseJson(jsonInput);
        Integer rowsCreated = DatabaseHelper.createPetByName(petData.name, "mysql");
        return new Rows(rowsCreated);
    }

    @Post("json")
    public Rows createMariaDB(String jsonInput) {
        PetCreate petData = parseJson(jsonInput);
        Integer rowsCreated = DatabaseHelper.createPetByName(petData.name, "mariadb");
        return new Rows(rowsCreated);
    }

    private PetCreate parseJson(String jsonInput) {
        // Simple JSON parsing (you can use a library like Jackson or Gson for more complex cases)
        String name = jsonInput.replaceAll(".*\"name\":\"([^\"]+)\".*", "$1");
        return new PetCreate(name);
    }
}
