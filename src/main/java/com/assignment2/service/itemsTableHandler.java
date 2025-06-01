package com.assignment2.service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.assignment2.gui_albert.AccountFormPage;
import com.assignment2.gui_albert.EditDialog;
import com.assignment2.gui_albert.FieldDefinition;
import com.assignment2.gui_albert.TablePage;
import com.assignment2.helpers.EditDialogContext;
import com.assignment2.helpers.JsonStorageHelper;
import com.assignment2.session.SessionManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class itemsTableHandler implements TableActionHandler{

    private JFrame currentPage;
    private TablePage page;
    private String filePath = "items.txt";
    private Map<String, JsonObject> originalDataMap = new LinkedHashMap<>();

    public itemsTableHandler(JFrame currentPage, TablePage page) {
        this.currentPage = currentPage;
        this.page = page;
    }
    
    @Override
    public void onAdd(){
        System.out.println("Error boss");
        throw new UnsupportedOperationException("onAdd unused");
    }

    @Override
    public void onEdit(JsonObject record){
        String itemName = record.get("Item").getAsString();

        // Find original row
        String itemId = JsonStorageHelper.lookupValueByLabel("items.txt", "itemName", "itemId", itemName);
        

        JsonArray poList;
        try {
            poList = JsonStorageHelper.loadAsJsonArray(filePath);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to load Items file.");
            return;
        }

        JsonObject original = null;
        for (JsonElement el : poList) {
            JsonObject obj = el.getAsJsonObject();
            String id = obj.get("itemId").getAsString();
            originalDataMap.put(id, obj);
            original = obj;
        }

        if (original == null) {
            JOptionPane.showMessageDialog(null, "Original record not found.");
            return;
        }

        Map<String, FieldDefinition> fieldDefs = new LinkedHashMap<>();
        fieldDefs.put("itemId", FieldDefinition
            .dropdown("items.txt", "itemName", "itemId")
            .withLabel("Item")
            .withKey("itemId")
            .required());

        fieldDefs.put("Stock Level", FieldDefinition
            .of("int")
            .withLabel("Stock Level")
            .withKey("stockLevel")
            .required());

        fieldDefs.put("Selling Price", FieldDefinition
            .of("int")
            .withLabel("Selling Price")
            .withKey("sellingPrice")
            .required());

        EditDialogContext context = new EditDialogContext();
        context.originalData = original.deepCopy();
        context.editedData = new JsonObject();
        context.editedData.addProperty("itemId", itemId);
        context.editedData.addProperty("Stock Level", original.get("stockLevel").getAsString());
        context.editedData.addProperty("Selling Price", original.get("sellingPrice").getAsString());
        context.tableName = "items";

        new EditDialog(null, updatedData -> {
            // Ensure poId is preserved from the original data
            String itemIdString = context.originalData.get("itemId").getAsString();
            updatedData.addProperty("itemId", itemIdString);

            try {
                JsonStorageHelper.updateOrInsert("items.txt", updatedData, "itemId");

                JsonArray updatedList = JsonStorageHelper.loadAsJsonArray("items.txt");
                page.refreshTableData(convert(updatedList));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed to update Items record.");
                e.printStackTrace();
            }
        }, fieldDefs, context).setVisible(true);
    }

    @Override
    public void onDelete(JsonObject rowData, String pointerKeyPath){
        throw new UnsupportedOperationException("onDelete unused");
    }

    public static JsonArray convert(JsonArray rawArray){
        // Initialize a new array to hold the converted objects
        JsonArray convertedArray = new JsonArray();

        for (JsonElement el : rawArray) {
            JsonObject original = el.getAsJsonObject(); // Original JSON object
            JsonObject converted = new JsonObject();    // New object to store converted data
            System.out.println(original);

            // Lookup and add item details based on itemId
            converted.addProperty("itemId", original.get("itemId").getAsInt());
            converted.addProperty("Item", getNameById("items.txt", "itemId", original.get("itemId").getAsInt(), "itemName"));
            converted.addProperty("stockLevel", getNameById("items.txt", "itemId", original.get("itemId").getAsInt(), "stockLevel"));
            converted.addProperty("sellingPrice", getNameById("items.txt", "itemId", original.get("itemId").getAsInt(), "sellingPrice"));

            // Add the converted object to the result array
            convertedArray.add(converted);
        }
        return convertedArray;
    }

    // Method to get the name by the Id
    private static String getNameById(String filePath, String idKey, int targetId, String nameKey) {
        try {
            JsonArray array = null;
            if(filePath.contains("users.txt")){
                JsonObject obj = JsonStorageHelper.loadAsJsonObject(filePath);

                array = obj.getAsJsonArray("users");
            }else{
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
}
