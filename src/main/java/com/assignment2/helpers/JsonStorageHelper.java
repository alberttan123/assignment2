package com.assignment2.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JsonStorageHelper {
    private static final Gson gson = new Gson();

    public static <T> T loadFromJson(String path, Class<T> type) throws IOException {
        try (FileReader reader = new FileReader(path)) {
            return gson.fromJson(reader, type);
        }
    }

    public static void saveToJson(String path, Object data) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(data, writer);
        }
    }

    public static JsonObject loadAsJsonObject(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            createDefaultUserFile(path);
        }

        try (FileReader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    public static JsonArray loadAsJsonArray(String filePath) throws IOException {
        Reader reader = Files.newBufferedReader(Path.of(filePath));
        return JsonParser.parseReader(reader).getAsJsonArray();
    }

    private static void createDefaultUserFile(String path) throws IOException {
        File file = new File(path);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()){
            parentDir.mkdirs();
        }

        JsonObject root = new JsonObject();
        JsonArray users = new JsonArray();

        users.add(createUser("admin@example.com", "asdf", "admin", "Admin", "Example", "", 1, 1, 1990, 1));
        users.add(createUser("attain938@gmail.com", "asdf", "admin", "Albert", "Tan", "", 23, 12, 2005, 2));
        users.add(createUser("financemanager", "asdf", "finance_manager", "Finance", "Guy", "", 3, 3, 1985, 3));
        users.add(createUser("purchasemanager", "asdf", "purchase_manager", "Purchase", "Pro", "", 4, 4, 1982, 4));
        users.add(createUser("inventorymanager", "asdf", "inventory_manager", "Inventory", "Boss", "", 5, 5, 1988, 5));
        users.add(createUser("salesmanager", "asdf", "sales_manager", "Sales", "Rep", "", 6, 6, 1980, 6));
        users.add(createUser("asdf", "asdf", "admin", "Testing", "Account", "", 23, 12, 2005, 7));

        root.add("users", users);

        saveToJson(path, root);
        System.out.println("Created default users.txt at: " + path);
    }

    private static JsonObject createUser(String email, String password, String role, String fname, String lname, String profilePicturePath, int day, int month, int year, int userId) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        JsonObject user = new JsonObject();
        user.addProperty("userId", userId);
        user.addProperty("email", email);
        user.addProperty("password", password);
        user.addProperty("role", role);
        user.addProperty("profilePicturePath", profilePicturePath);
        user.addProperty("createdAt", now);

        JsonObject nameObj = new JsonObject();
        nameObj.addProperty("fname", fname);
        nameObj.addProperty("lname", lname);
        user.add("name", nameObj);

        JsonObject dobObj = new JsonObject();
        dobObj.addProperty("day", String.valueOf(day));
        dobObj.addProperty("month", String.valueOf(month));
        dobObj.addProperty("year", String.valueOf(year));
        user.add("dob", dobObj);

        return user;
    }

    public static int getNextUserId() {
        try {
            JsonObject root = JsonStorageHelper.loadAsJsonObject("data/users.txt");
            JsonArray users = root.getAsJsonArray("users");

            int maxId = 0;
            for (JsonElement elem : users) {
                JsonObject user = elem.getAsJsonObject();
                if (user.has("userId") && !user.get("userId").isJsonNull()) {
                    int id = user.get("userId").getAsInt();
                    if (id > maxId) {
                        maxId = id;
                    }
                }
            }
            return maxId + 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 1; // fallback
        }
    }
}
