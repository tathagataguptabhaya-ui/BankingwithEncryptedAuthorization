package com.example.mavenproject3;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import java.util.Scanner;
public class hashpwd {

    public static void main(String[] args) {
        // Parameters: saltLength, hashLength, parallelism, memory, iterations
        // These are standard "safe" defaults for 2026:
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16, 32, 1, 65536, 10);
        Scanner sc = new Scanner(System.in);
        String rawPassword = sc.nextLine();
        
        // 1. Hash the password
        String hashedPassword = encoder.encode(rawPassword);
        System.out.println("Argon2id Hash: " + hashedPassword);

        // 2. Verify the password
        boolean isMatch = encoder.matches(rawPassword, hashedPassword);
        System.out.println("Login Success: " + isMatch);
    }
}