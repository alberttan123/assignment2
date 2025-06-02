package com.assignment2.gui_albert;

import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.assignment2.helpers.JsonStorageHelper;
import com.assignment2.service.LoginService;
import com.assignment2.session.SessionManager;
import com.google.gson.JsonObject;

public class LoginPage extends GUI {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;

    private File logoPath;

    public LoginPage(){
        this.windowWidth = 300;
        this.windowHeight = 200;
        initWindow("Login");
        render();
        handleEvents();
    }

    @Override
    public void render() {
        System.out.println("Rendering Login Page...");

        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(loginButton);

        add(panel);
    }

    @Override
    public void handleEvents() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());

                int success = LoginService.validateLogin(email, password);
                
                if (email.equals("")){
                    JOptionPane.showMessageDialog(null, "Please enter an email.");
                } else if (success == 1) {
                    System.out.println("Login Successful.");

                    JsonObject user = LoginService.getUserDetails(email);
                    SessionManager.setCurrentUser(user);
                    
                    HomePage homePage = new HomePage();
                    homePage.setVisible(true);
                    dispose(); // close login window
                } else if (success == 2){
                    System.out.println("First Time Login.");
                    JsonObject user = LoginService.getUserDetails(email);

                    SessionManager.setCurrentUser(user);
                    FirstLoginModal.showModal(LoginPage.this, email);

                    System.out.println("Login Successful.");
                    SessionManager.setCurrentUser(user);
                    
                    HomePage homePage = new HomePage();
                    homePage.setVisible(true);
                    dispose(); // close login window
                } else {
                    System.out.println("Login failed. This could be due to incorrect email/password.");
                    JOptionPane.showMessageDialog(null, "Login failed.\nThis could be due to incorrect email/password.");
                }
            }
        });
    }

    @Override
    public void updateState() {
        // Update logic if needed
    }
}
