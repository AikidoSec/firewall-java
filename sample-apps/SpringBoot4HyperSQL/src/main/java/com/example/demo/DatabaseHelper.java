package com.example.demo;

import javax.sql.DataSource;
import org.hsqldb.jdbc.JDBCDataSource;
import com.example.demo.models.Pet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseHelper {
    static DataSource createDataSource() {
        JDBCDataSource dataSource = new JDBCDataSource();

        // For in-memory database (data is lost when the application stops)
        dataSource.setURL("jdbc:hsqldb:mem:mydatabase");
        dataSource.setUser("SA");
        dataSource.setPassword("");
        return dataSource;
    }

    public static ArrayList<Object> getAllPets() {
        ArrayList<Object> pets = new ArrayList<>();
        DataSource db = createDataSource();
        try {
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM pets");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt("pet_id");
                String name = rs.getString("pet_name");
                String owner = rs.getString("owner");
                pets.add(new Pet(id, name, owner));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pets;
    }
    public static Pet getPetById(Integer id) {
        ArrayList<Object> pets = new ArrayList<>();
        DataSource db = createDataSource();
        try {
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM pets WHERE pet_id=?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Integer pet_id = rs.getInt("pet_id");
                String name = rs.getString("pet_name");
                String owner = rs.getString("owner");
                return new Pet(pet_id, name, owner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Pet(0, "Unknown", "Unknown");
    }
    public static Integer createPetByName(String pet_name) {
        String sql = "INSERT INTO pets (pet_name, owner) VALUES ('" + pet_name  + "', 'Aikido Security')";
        DataSource db = createDataSource();
        try {
            Connection conn = db.getConnection();
            PreparedStatement insertStmt = conn.prepareStatement(sql);
            return insertStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
