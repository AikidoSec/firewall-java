package com.example.demo.utils

import com.example.demo.models.Pet
import org.postgresql.ds.PGSimpleDataSource
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import javax.sql.DataSource

object DatabaseHelper {
    // Create and return a DataSource for our Postgres DB
    private fun createDataSource(): DataSource {
        // The url specifies the address of our database along with username and password credentials
        // you should replace these with your own username and password
        val url = "jdbc:postgresql://localhost:5432/db?user=user&password=password"
        return PGSimpleDataSource().apply {
            setURL(url)
        }
    }

    fun getAllPets(): List<Pet> {
        val pets = mutableListOf<Pet>()
        val db = createDataSource()
        try {
            db.connection.use { conn ->
                val stmt: PreparedStatement = conn.prepareStatement("SELECT * FROM pets")
                val rs: ResultSet = stmt.executeQuery()
                while (rs.next()) {
                    val id = rs.getInt("pet_id")
                    val name = rs.getString("pet_name")
                    val owner = rs.getString("owner")
                    pets.add(Pet(id, name, owner))
                }
            }
        } catch (ignored: SQLException) {
            // Handle exception if needed
        }
        return pets
    }

    fun getPetById(id: Int): Pet {
        val db = createDataSource()
        try {
            db.connection.use { conn ->
                val stmt: PreparedStatement = conn.prepareStatement("SELECT * FROM pets WHERE pet_id=?")
                stmt.setInt(1, id)
                val rs: ResultSet = stmt.executeQuery()
                if (rs.next()) {
                    val petId = rs.getInt("pet_id")
                    val name = rs.getString("pet_name")
                    val owner = rs.getString("owner")
                    return Pet(petId, name, owner)
                }
            }
        } catch (ignored: SQLException) {
            // Handle exception if needed
        }
        return Pet(0, "Unknown", "Unknown")
    }

    fun createPetByName(petName: String): Int {
        val sql = "INSERT INTO pets (pet_name, owner) VALUES ('$petName', 'Aikido Security')"
        val db = createDataSource()
        return try {
            db.connection.use { conn ->
                val insertStmt: PreparedStatement = conn.prepareStatement(sql)
                insertStmt.executeUpdate()
            }
        } catch (ignored: SQLException) {
            // Handle exception if needed
            0
        }
    }
}
