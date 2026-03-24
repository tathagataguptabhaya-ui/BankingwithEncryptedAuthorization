package com.example.mavenproject3;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField userField;
    private JPasswordField passField;
    private JButton loginBtn, registerBtn;
    private final UserDAO userDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        initComponents();
    }

    private void initComponents() {
        setTitle("Welcome to West Bengal STate Bank");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Using a Panel with Padding for a better look
        JPanel mainPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(new JLabel("Username:"));
        userField = new JTextField();
        mainPanel.add(userField);

        mainPanel.add(new JLabel("Password:"));
        passField = new JPasswordField();
        mainPanel.add(passField);

        loginBtn = new JButton("Login");
        registerBtn = new JButton("Register New User");

        mainPanel.add(loginBtn);
        mainPanel.add(registerBtn);

        add(mainPanel);

        // --- Action Listeners ---

        loginBtn.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());
            if (userDAO.loginUser(user, pass)) {
                JOptionPane.showMessageDialog(this, "Welcome back, " + user + "!");
                // Open Dashboard here
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerBtn.addActionListener(e -> {
            try {
                String user = userField.getText();
                String pass = new String(passField.getPassword());
                
                if (user.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill all fields.");
                    return;
                }
                
                if (userDAO.registerUser(user, pass)) {
                    JOptionPane.showMessageDialog(this, "Customer registered successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed (User might already exist).");
                }
            } catch (Exception ex) {
                System.getLogger(LoginFrame.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}