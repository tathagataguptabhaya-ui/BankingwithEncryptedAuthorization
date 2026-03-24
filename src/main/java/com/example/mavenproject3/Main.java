package com.example.mavenproject3;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserDAO userDAO = new UserDAO();

        System.out.println("--- Welcome to West Bengal Bank Banking Services---");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.print("Choose an option: ");
        
        int choice = sc.nextInt();
        sc.nextLine(); // Consume newline

        System.out.print("Enter Username: ");
        String user = sc.nextLine();
        System.out.print("Enter Password: ");
        String pass = sc.nextLine();

        if (choice == 1) {
            try {
                if (userDAO.registerUser(user, pass)) {
                    System.out.println("Registration successful!");
                } else {
                    System.out.println("Registration failed.");
                }
            } catch (Exception ex) {
                System.getLogger(Main.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        } else if (choice == 2) {
            if (userDAO.loginUser(user, pass)) {
                System.out.println("Login successful! Welcome, " + user);
                // Launch Pharmacy Dashboard here
            } else {
                System.out.println("Invalid username or password.");
            }
        }
    }
}