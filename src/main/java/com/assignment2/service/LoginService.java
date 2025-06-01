package com.assignment2.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import com.assignment2.helpers.JsonStorageHelper;

public class LoginService {
    private static final String USER_DATA_PATH = "data/users.txt";

    public static int validateLogin(String email, String password) {
        try {
            JsonObject root = JsonStorageHelper.loadAsJsonObject(USER_DATA_PATH);
            JsonArray users = root.getAsJsonArray("users");

            for (int i = 0; i < users.size(); i++) {
                JsonObject user = users.get(i).getAsJsonObject();
                if (user.get("email").getAsString().equals(email) && user.get("password").getAsString().isEmpty()){
                    return 2; //new account - opens first time login modal
                }
                if (user.get("email").getAsString().equals(email) &&
                    user.get("password").getAsString().equals(password)) {
                    return 1; //successful login
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0; //failed login
    }

    public static JsonObject getUserDetails(String email) {
        try {
            JsonObject root = JsonStorageHelper.loadAsJsonObject(USER_DATA_PATH);
            JsonArray users = root.getAsJsonArray("users");

            for (int i = 0; i < users.size(); i++) {
                JsonObject user = users.get(i).getAsJsonObject();
                if (user.get("email").getAsString().equals(email)) {
                    return user;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
