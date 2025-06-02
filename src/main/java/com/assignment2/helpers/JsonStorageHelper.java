package com.assignment2.helpers;

import com.google.gson.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Predicate;

public class JsonStorageHelper {
    private static final Path PROJECT_BASE = Paths.get(System.getProperty("user.dir"));
    public static final Path DATA_DIR = PROJECT_BASE.resolve("data");
    private static final Gson gson = new Gson();

    static {
        try {
            Files.createDirectories(DATA_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data directory", e);
        }
    }

    // ---------- Unified path resolution ----------
    private static Reader resolveReader(String fileName) throws IOException {
        Path resolvedPath = DATA_DIR.resolve(fileName);

        if (Files.exists(resolvedPath)) {
            return Files.newBufferedReader(resolvedPath);
        }

        // Optional fallback to resources
        InputStream stream = JsonStorageHelper.class.getClassLoader().getResourceAsStream("data/" + fileName);
        if (stream != null) {
            return new InputStreamReader(stream);
        }

        throw new FileNotFoundException("File not found: " + resolvedPath.toAbsolutePath());
    }

    // ---------- General JSON loading ----------
    public static JsonObject loadAsJsonObject(String filePath) throws IOException {
        try (Reader reader = resolveReader(filePath)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    public static JsonArray loadAsJsonArray(String filePath) throws IOException {
        try (Reader reader = resolveReader(filePath)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        }
    }

    public static <T> T loadFromJson(String path, Class<T> type) throws IOException {
        try (Reader reader = resolveReader(path)) {
            return gson.fromJson(reader, type);
        }
    }

    public static void saveToJson(String fileName, Object data) throws IOException {
        Path target = DATA_DIR.resolve(fileName);
        Files.createDirectories(target.getParent());
        try (Writer writer = Files.newBufferedWriter(target)) {
            gson.toJson(data, writer);
        }
    }

    // ---------- Update logic ----------
    public static void updateOrInsert(String filePath, JsonObject updatedData, String matchingIdField) throws IOException {
        JsonArray array = loadAsJsonArray(filePath);
        boolean updated = false;

        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = array.get(i).getAsJsonObject();
            if (obj.has(matchingIdField) && obj.get(matchingIdField).getAsString().equals(updatedData.get(matchingIdField).getAsString())) {
                for (Map.Entry<String, JsonElement> entry : updatedData.entrySet()) {
                    obj.add(entry.getKey(), entry.getValue());
                }
                updated = true;
                break;
            }
        }

        if (!updated) array.add(updatedData);
        saveToJson(filePath, array);
    }

    // ---------- Utility lookups ----------
    public static boolean rowExists(String path, Predicate<JsonObject> condition) {
        try {
            JsonArray array = loadAsJsonArray(path);
            for (JsonElement el : array) {
                if (condition.test(el.getAsJsonObject())) return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String lookupValueByLabel(String file, String labelField, String valueField, String targetLabel) {
        try {
            JsonArray source = loadAsJsonArray(file);
            for (JsonElement el : source) {
                JsonObject obj = el.getAsJsonObject();
                if (obj.has(labelField) && obj.get(labelField).getAsString().equals(targetLabel)) {
                    return obj.get(valueField).getAsString();
                }
            }
        } catch (IOException e) {
            System.out.println("File not found: " + file);
            e.printStackTrace();
        }
        return null;
    }

    // ---------- User scaffolding ----------
    public static void createDefaultUserFile(String path) throws IOException {
        File file = new File(path);
        if (file.getParentFile() != null) file.getParentFile().mkdirs();

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

    private static JsonObject createUser(String email, String password, String role, String fname, String lname,
                                         String profilePicturePath, int day, int month, int year, int userId) {
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
            JsonObject root = loadAsJsonObject("data/users.txt");
            JsonArray users = root.getAsJsonArray("users");
            int maxId = 0;
            for (JsonElement elem : users) {
                JsonObject user = elem.getAsJsonObject();
                if (user.has("userId") && !user.get("userId").isJsonNull()) {
                    maxId = Math.max(maxId, user.get("userId").getAsInt());
                }
            }
            return maxId + 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
    }
}