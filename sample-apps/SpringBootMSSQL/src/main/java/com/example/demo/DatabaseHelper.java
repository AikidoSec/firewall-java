package com.example.demo;

import com.example.demo.models.Pet;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseHelper {
    // We can create a method to create and return a DataSource for our Postgres DB
    private static Connection  createDataConn() {
        // The url specifies the address of our database along with username and password credentials
        // you should replace these with your own username and password
        String url = "jdbc:sqlserver://localhost:1433;databaseName=db;encrypt=false"; // Change to your database
        String user = "sa"; // Change to your username
        String password = "Strong!Passw0rd"; // Change to your password
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Exception in createDataSource(): " + e);
        }
        return null;
    }
    public static ArrayList<Object> getAllPets() {
        ArrayList<Object> pets = new ArrayList<>();
        try {
            Connection conn = createDataConn();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM dbo.pets;");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt("pet_id");
                String name = rs.getString("pet_name");
                String owner = rs.getString("owner");
                pets.add(new Pet(id, name, owner));
            }
        } catch (SQLException ignored) {
        }
        return pets;
    }
    public static Pet getPetById(Integer id) {
        ArrayList<Object> pets = new ArrayList<>();
        try {
            Connection conn = createDataConn();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM pets WHERE pet_id=?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Integer pet_id = rs.getInt("pet_id");
                String name = rs.getString("pet_name");
                String owner = rs.getString("owner");
                return new Pet(pet_id, name, owner);
            }
        } catch (SQLException ignored) {
        }
        return new Pet(0, "Unknown", "Unknown");
    }
    public static Integer createPetByName(String pet_name) {
        String sql = "INSERT INTO pets (pet_name, owner) VALUES ('" + pet_name  + "', 'Aikido Security')";
        try {
            Connection conn = createDataConn();
            PreparedStatement insertStmt = conn.prepareStatement(sql);
            return insertStmt.executeUpdate();
        } catch (SQLException ignored) {
        }
        return 0;
    }
}
