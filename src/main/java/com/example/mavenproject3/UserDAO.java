package com.example.mavenproject3;

import java.sql.*;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class UserDAO {
    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/wb_state_bank";
    private static final String USER = "root";
    private static final String PASS = "root";

    // Initialize the encoder with the same parameters used during registration
    private static final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16, 32, 1, 65536, 10);

    // Method to Register a New User
    public boolean registerUser(String username, String rawPassword) {
        String query = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            String hashed = encoder.encode(rawPassword); // Hash it!
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashed);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to Login/Verify a User
    public boolean loginUser(String username, String rawPassword) {
        String query = "SELECT password_hash FROM users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                // The encoder automatically extracts the salt and parameters from the storedHash
                return encoder.matches(rawPassword, storedHash);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}