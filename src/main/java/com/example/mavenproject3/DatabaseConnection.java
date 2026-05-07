package com.example.mavenproject3;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseConnection {
    private static Properties props = new Properties();

    static {
        try (InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                System.out.println("Sorry, unable to find db.properties");
            } else {
                props.load(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(
            props.getProperty("db.url"),
            props.getProperty("db.user"),
            props.getProperty("db.password")
        );
    }
}