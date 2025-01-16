package com.example.demo.utils

import javax.sql.DataSource
import com.example.demo.models.Pet
import org.postgresql.ds.PGSimpleDataSource

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class DatabaseHelper {
    // Method to create and return a DataSource for our Postgres DB
    private static DataSource createDataSource() {
        // The url specifies the address of our database along with username and password credentials
        // you should replace these with your own username and password
        final String url = "jdbc:postgresql://localhost:5432/db?user=user&password=password"
        final PGSimpleDataSource dataSource = new PGSimpleDataSource()
        dataSource.setUrl(url)
        return dataSource
    }

    static List<Pet> getAllPets() {
        List<Pet> pets = []
        DataSource db = createDataSource()
        try {
            Connection conn = db.getConnection()
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM pets")
            ResultSet rs = stmt.executeQuery()
            while (rs.next()) {
                Integer id = rs.getInt("pet_id")
                String name = rs.getString("pet_name")
                String owner = rs.getString("owner")
                pets.add(new Pet(id, name, owner))
            }
        } catch (SQLException ignored) {
            // Handle exception if needed
        }
        return pets
    }

    static Pet getPetById(Integer id) {
        DataSource db = createDataSource()
        try {
            Connection conn = db.getConnection()
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM pets WHERE pet_id=?")
            stmt.setInt(1, id)
            ResultSet rs = stmt.executeQuery()
            if (rs.next()) {
                Integer petId = rs.getInt("pet_id")
                String name = rs.getString("pet_name")
                String owner = rs.getString("owner")
                return new Pet(petId, name, owner)
            }
        } catch (SQLException ignored) {
            // Handle exception if needed
        }
        return new Pet(0, "Unknown", "Unknown")
    }

    static Integer createPetByName(String petName) {
        String sql = "INSERT INTO pets (pet_name, owner) VALUES ('" + petName  + "', 'Aikido Security')";
        DataSource db = createDataSource()
        try {
            Connection conn = db.getConnection()
            PreparedStatement insertStmt = conn.prepareStatement(sql)
            return insertStmt.executeUpdate()
        } catch (SQLException ignored) {
            // Handle exception if needed
        }
        return 0
    }
}
