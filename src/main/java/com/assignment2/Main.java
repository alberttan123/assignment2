package com.assignment2;

import javax.swing.SwingUtilities;

import com.assignment2.gui_albert.LoginPage;


public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginPage().setVisible(true);
        });
    }
}
