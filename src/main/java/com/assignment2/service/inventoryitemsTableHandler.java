package com.assignment2.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.assignment2.gui_albert.AddPage;
import com.assignment2.gui_albert.EditDialog;
import com.assignment2.gui_albert.FieldDefinition;
import com.assignment2.gui_albert.TablePage;
import com.assignment2.helpers.EditDialogContext;
import com.assignment2.helpers.JsonStorageHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class inventoryitemsTableHandler implements TableActionHandler {

    private JFrame currentPage;
    private TablePage page;
    private String filePath = "items.txt";
    private Map<String, JsonObject> originalDataMap = new LinkedHashMap<>();

    public inventoryitemsTableHandler(JFrame currentPage, TablePage page) {
        this.currentPage = currentPage;
        this.page = page;
    }

    @Override
    public void onAdd() {
        LinkedHashMap<String, String> fieldLabels = new LinkedHashMap<>();
        fieldLabels.put("Stock Level", "stockLevel");

        Map<String, String> dataTypes = Map.of(
                "stockLevel", "int"
        );

        Map<String, Object> fieldOptions = new HashMap<>();
        Map<String, Function<String, Boolean>> validators = new HashMap<>();

        String primaryKey = "itemId";

        AddPage dialog = new AddPage(currentPage, filePath, fieldLabels, dataTypes, fieldOptions, validators, primaryKey);

        // Refresh after dialog closes
        JsonArray updatedList = getLatestData();
        ((TablePage) currentPage).refreshTableData(convert(updatedList));
    }

    @Override
    public void onEdit(JsonObject record) {
        // Extract the item name from the input JSON record
        String itemName = record.get("Item").getAsString();
        String itemId = record.get("Item Id").getAsString();

        JsonArray itemList;
        try {
            // Load the current list of items from filepath
            itemList = JsonStorageHelper.loadAsJsonArray(filePath);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to load Items file.");
            return;
        }

        // Iterate through the loaded list to find the original record using itemId
        JsonObject original = null;
        for (JsonElement el : itemList) {
            JsonObject obj = el.getAsJsonObject();
            String id = obj.get("itemId").getAsString();
            originalDataMap.put(id, obj);

            // If itemId matches, mark it as the original record
            if (id.equals(itemId)) {
                original = obj;
                break;
            }
        }

        // If no matching original record was found, show an error
        if (original == null) {
            JOptionPane.showMessageDialog(null, "Original record not found.");
            return;
        }

        Map<String, FieldDefinition> fieldDefs = new LinkedHashMap<>();

        // Only allow Stock Level to be edited
        fieldDefs.put("stockLevel", FieldDefinition
                .of("int")
                .withLabel("Stock Level")
                .withKey("stockLevel")
                .required());

        // Set up the edit context with original and editable data
        EditDialogContext context = new EditDialogContext();
        context.originalData = original.deepCopy();
        context.editedData = new JsonObject();

        // Pre-fill the editable fields with original data
        context.editedData.addProperty("itemId", itemId);
        context.editedData.addProperty("stockLevel", original.get("stockLevel").getAsString());
        context.editedData.addProperty("sellingPrice", original.get("sellingPrice").getAsString());
        context.editedData.addProperty("itemName", original.get("itemName").getAsString());
        context.tableName = "items";

        new EditDialog(null, updatedData -> {
            // Ensure all original data is preserved except stock level
            String itemIdString = context.originalData.get("itemId").getAsString();
            String sellingPrice = context.originalData.get("sellingPrice").getAsString();
            String originalItemName = context.originalData.get("itemName").getAsString();

            updatedData.addProperty("itemId", itemIdString);
            updatedData.addProperty("sellingPrice", sellingPrice);
            updatedData.addProperty("itemName", originalItemName);

            try {
                // Update the items.txt file with the new data
                JsonStorageHelper.updateOrInsert("items.txt", updatedData, "itemId");

                // Reload updated data and refresh the UI table
                JsonArray updatedList = JsonStorageHelper.loadAsJsonArray("items.txt");
                page.refreshTableData(convert(updatedList));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed to update Items record.");
                e.printStackTrace();
            }
        }, fieldDefs, context).setVisible(true); // Show the dialog
    }

    @Override
    public void onDelete(JsonObject rowData, String pointerKeyPath) {
        // Extract the value of the itemId from the row that is being deleted
        String keyVal = rowData.get("itemId").getAsString();
        // Call helper method to remove the item from the JSON file based on itemId
        deleteRowFromJson(keyVal, pointerKeyPath);
        System.out.println("Deleted item.");
        page.refreshTableData(getLatestData());
    }

    // Method to load latest data from items.txt and return it as JsonArray
    private JsonArray getLatestData() {
        String jsonFilePath = "items.txt";
        JsonArray root = null;
        try {
            // Load the entire file as a JsonArray
            root = JsonStorageHelper.loadAsJsonArray(jsonFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;// Return the loaded data
    }

    public static JsonArray convert(JsonArray rawArray) {
        // Initialize a new array to hold the converted objects
        JsonArray convertedArray = new JsonArray();

        for (JsonElement el : rawArray) {
            JsonObject original = el.getAsJsonObject(); // Original JSON object
            JsonObject converted = new JsonObject();    // New object to store converted data
            System.out.println(original);

            // Lookup and add item details based on itemId
            converted.addProperty("Item Id", original.get("itemId").getAsInt());
            converted.addProperty("Item", getNameById("items.txt", "itemId", original.get("itemId").getAsInt(), "itemName"));
            converted.addProperty("Stock Level", getNameById("items.txt", "itemId", original.get("itemId").getAsInt(), "stockLevel"));
            converted.addProperty("Selling Price", getNameById("items.txt", "itemId", original.get("itemId").getAsInt(), "sellingPrice"));

            // Add the converted object to the result array
            convertedArray.add(converted);
        }
        return convertedArray;
    }

    // Method to get the name by the Id
    private static String getNameById(String filePath, String idKey, int targetId, String nameKey) {
        try {
            JsonArray array = null;
            if (filePath.contains("users.txt")) {
                JsonObject obj = JsonStorageHelper.loadAsJsonObject(filePath);

                array = obj.getAsJsonArray("users");
            } else {
                array = JsonStorageHelper.loadAsJsonArray(filePath);
            }
            for (JsonElement el : array) {
                JsonObject obj = el.getAsJsonObject();
                if (obj.get(idKey).getAsInt() == targetId) {
                    return obj.get(nameKey).getAsString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    // Delete a row from a JSON array using keyValue
    private void deleteRowFromJson(String keyValue, String pointerKeyPath) {
        try {
            JsonArray arr = getLatestData(); // e.g., array of items

            for (int i = 0; i < arr.size(); i++) {
                JsonObject obj = arr.get(i).getAsJsonObject();

                // Retrieve the value of the field based on the pointerKeyPath
                JsonElement element = obj.get(pointerKeyPath);
                if (element == null) {
                    continue;
                }

                String value = element.getAsString();
                if (value.equals(keyValue)) {
                    arr.remove(i); // Remove the object at index i

                    // Log the file path and the updated content
                    System.out.println("Deleting item with " + pointerKeyPath + " = " + keyValue);
                    System.out.println("Saving changes to file: " + "items.txt");

                    JsonStorageHelper.saveToJson("items.txt", arr); // Save updated data

                    System.out.println("Data successfully saved to " + "items.txt");
                    // JsonStorageHelper.saveToJson(filePath, arr); // Save updated array back into the file
                    break;
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(currentPage, "Failed to delete row from JSON.");
            e.printStackTrace();
        }
    }
}
