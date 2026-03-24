package com.example.mavenproject3;

import java.sql.*;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class UserDAO {
    

    // Initialize the encoder with the same parameters used during registration
    private static final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16, 32, 1, 65536, 10);

    // Method to Register a New User
    public boolean registerUser(String username, String rawPassword) throws Exception {
        String query = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
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

    public boolean loginUser(String username, String rawPassword) {
        String query = "SELECT password_hash FROM users WHERE username = ?";
    
        // Use the utility to get the connection
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
        
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
        
                if (rs.next()) {
                    return encoder.matches(rawPassword, rs.getString("password_hash"));
                }
        } catch (Exception e) {
            e.printStackTrace();
            }
        return false;
    }
}