package com.example.demo;
import javax.sql.DataSource;

import com.example.demo.models.Pet;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseHelper {
    private static Connection getDBConnection() {
        // The url specifies the address of our database along with username and password credentials
        final String url = "jdbc:sqlite://localhost:3306/db";
        final String user = "user";
        final String password = "password";
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Exception occurred in getDBConnection(): " + e);
            return null;
        }
    }

    public static ArrayList<Object> getAllPets() {
        ArrayList<Object> pets = new ArrayList<>();
        Connection conn = getDBConnection();
        if (conn == null) {
            return pets;
        }
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM pets");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt("pet_id");
                String name = rs.getString("pet_name");
                String owner = rs.getString("owner");
                pets.add(new Pet(id, name, owner));
            }
        } catch (SQLException e) {
            System.out.println("Exception occurred in getAllPets(): " + e);
        }
        return pets;
    }
    public static Pet getPetById(Integer id) {
        ArrayList<Object> pets = new ArrayList<>();
        Connection conn = getDBConnection();
        if (conn == null) {
            return null;
        }
        try {
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
            System.out.println("Exception occurred in getPetById(...): " + e);
        }
        return new Pet(0, "Unknown", "Unknown");
    }
    public static Integer createPetByName(String pet_name) {
        String sql = "INSERT INTO pets (pet_name, owner) VALUES (\"" + pet_name  + "\", \"Aikido Security\")";
        Connection conn = getDBConnection();
        if (conn == null) {
            return 0;
        }
        try {
            PreparedStatement insertStmt = conn.prepareStatement(sql);
            return insertStmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Exception occurred in createPetByName(...): " + e);
        }
        return 0;
    }
}
