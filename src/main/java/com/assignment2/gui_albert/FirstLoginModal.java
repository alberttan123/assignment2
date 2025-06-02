package com.assignment2.gui_albert;

import javax.swing.*;

import com.assignment2.helpers.JsonStorageHelper;
import com.assignment2.session.SessionManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class FirstLoginModal extends JDialog {
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton confirmButton;
    private String email;

    public FirstLoginModal(JFrame parent, String email) {
        super(parent, "First Time Login", true); // modal
        setSize(350, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setAlwaysOnTop(true);

        this.email = email;

        render();
        handleEvents();
    }

    public void render() {
        System.out.println("Rendering First Time Login Modal...");
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JPanel fieldsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();
        confirmButton = new JButton("CONFIRM");

        fieldsPanel.add(labeled("New Password", newPasswordField));
        fieldsPanel.add(labeled("Re-enter New Password", confirmPasswordField));
        fieldsPanel.add(confirmButton);

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    public void handleEvents() {
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleConfirm();
            }
        });
    }

    private JPanel labeled(String label, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label), BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private void handleConfirm() {
        String pass1 = new String(newPasswordField.getPassword());
        String pass2 = new String(confirmPasswordField.getPassword());

        if (pass1.isEmpty() || pass2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in both password fields.");
            return;
        }

        if (!pass1.equals(pass2)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        try {
            JsonObject root = JsonStorageHelper.loadAsJsonObject("data/users.txt");
            JsonArray users = root.getAsJsonArray("users");

            for (int i = 0; i < users.size(); i++) {
                JsonObject user = users.get(i).getAsJsonObject();
                if (user.get("email").getAsString().equalsIgnoreCase(email)) {
                    user.addProperty("password", pass1);
                    break;
                }
            }

            JsonStorageHelper.saveToJson("data/users.txt", root);
            JOptionPane.showMessageDialog(this, "Password successfully set.");
            dispose();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save password.");
            ex.printStackTrace();
        }
    }

    public static void showModal(JFrame parent, String email) {
        FirstLoginModal modal = new FirstLoginModal(parent, email);
        modal.setVisible(true);
    }
}