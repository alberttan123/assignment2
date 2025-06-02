package com.assignment2.session;

import java.awt.Image;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.ImageIcon;

import com.assignment2.helpers.JsonStorageHelper;
import com.google.gson.JsonObject;

public class SessionManager {
    private static JsonObject currentUser;

    public static void setCurrentUser(JsonObject user) {
        currentUser = user;
    }

    public static JsonObject getCurrentUser() {
        return currentUser;
    }

    public static String getFullName() {
        JsonObject nameObject = currentUser.getAsJsonObject("name");
        String fullName = nameObject.get("fname").getAsString() + " " + nameObject.get("lname").getAsString();
        return currentUser != null ? fullName : null;
    }

    public static String getFirstName() {
        JsonObject nameObject = currentUser.getAsJsonObject("name");
        return currentUser != null ? nameObject.get("fname").getAsString() : null;
    }

    public static String getLastName() {
        JsonObject nameObject = currentUser.getAsJsonObject("name");
        return currentUser != null ? nameObject.get("lname").getAsString() : null;
    }

    public static String getUserEmail() {
        return currentUser != null ? currentUser.get("email").getAsString() : null;
    }

    public static String getUserRole() {
        return currentUser != null ? currentUser.get("role").getAsString() : null;
    }

    public static String getBirthDay() {
        JsonObject nameObject = currentUser.getAsJsonObject("dob");
        return currentUser != null ? nameObject.get("day").getAsString() : null;
    }

    public static String getBirthMonth() {
        JsonObject nameObject = currentUser.getAsJsonObject("dob");
        return currentUser != null ? nameObject.get("month").getAsString() : null;
    }

    public static String getBirthYear() {
        JsonObject nameObject = currentUser.getAsJsonObject("dob");
        return currentUser != null ? nameObject.get("year").getAsString() : null;
    }

    public static String getGUIUserRole() {
        if (currentUser == null || !currentUser.has("role")) return null;

        String role = currentUser.get("role").getAsString();

        switch (role.toLowerCase()) {
            case "admin":
                return "Administrator";
            case "finance_manager":
                return "Finance Manager";
            case "purchase_manager":
                return "Purchase Manager";
            case "inventory_manager":
                return "Inventory Manager";
            case "sales_manager":
                return "Sales Manager";
            default:
                // Fallback - returns role directly
                return role;
        }
    }

    public static Image getPfp() {
        String profilePicturePath = currentUser.get("profilePicturePath").getAsString();

        if (profilePicturePath == null || profilePicturePath.isBlank()) {
            System.err.println("No profile picture path specified.");
            return null;
        }

        // Use same base path as JsonStorageHelper
        Path imagePath = JsonStorageHelper.getDataPath(profilePicturePath);

        if (Files.exists(imagePath)) {
            try {
                ImageIcon icon = new ImageIcon(imagePath.toString());
                return icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Fallback to classpath resource
        URL imageUrl = SessionManager.class.getClassLoader().getResource("data/" + profilePicturePath);
        if (imageUrl != null) {
            ImageIcon icon = new ImageIcon(imageUrl);
            return icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        }

        System.err.println("Profile picture not found: " + profilePicturePath);
        return null;
    }

    public static boolean checkPfpExists(){
        return !currentUser.get("profilePicturePath").getAsString().equals("");
    }

    public static void reloadCurrentUser() {
        if (currentUser == null) return;

        String email = getUserEmail();

        try {
            JsonObject root = com.assignment2.helpers.JsonStorageHelper.loadAsJsonObject("/data/users.txt");
            for (var userElement : root.getAsJsonArray("users")) {
                JsonObject user = userElement.getAsJsonObject();
                if (user.get("email").getAsString().equalsIgnoreCase(email)) {
                    setCurrentUser(user);
                    System.out.println("Session reloaded for: " + email);
                    return;
                }
            }
            System.out.println("User not found for session reload: " + email);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clear() {
        System.out.println("Clearing Session Info...");
        currentUser = null;
    }
}
