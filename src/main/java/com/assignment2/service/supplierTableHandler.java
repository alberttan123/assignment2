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

public class supplierTableHandler implements TableActionHandler{
    private JFrame currentPage;
    private TablePage page;
    private String filePath = "Supplier.txt";
    private Map<String, JsonObject> originalDataMap = new LinkedHashMap<>();

    public supplierTableHandler(JFrame currentPage, TablePage page) {
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
        // Extract the item name from the input JSON record
        String itemName = record.get("Item").getAsString();

        // Lookup the itemId using itemName from the file named items.txt
        String itemId = JsonStorageHelper.lookupValueByLabel("items.txt", "itemName", "itemId", itemName);
        

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
            original = obj;
        }

        // If no matching original record was found, show an error
        if (original == null) {
            JOptionPane.showMessageDialog(null, "Original record not found.");
            return;
        }

        Map<String, FieldDefinition> fieldDefs = new LinkedHashMap<>();
        // Field for Item Name
        fieldDefs.put("itemId", FieldDefinition
            .dropdown("items.txt", "itemName", "itemId")
            .withLabel("Item")
            .withKey("itemId")
            .required());

        // Field for Stock Level
        fieldDefs.put("Stock Level", FieldDefinition
            .of("int")
            .withLabel("Stock Level")
            .withKey("stockLevel")
            .required());

        // Field for Selling Price
        fieldDefs.put("Selling Price", FieldDefinition
            .of("int")
            .withLabel("Selling Price")
            .withKey("sellingPrice")
            .required());

        // Set up the edit context with original and editable data
        EditDialogContext context = new EditDialogContext();
        context.originalData = original.deepCopy();
        context.editedData = new JsonObject();

        // Pre-fill the editable fields with original data
        context.editedData.addProperty("itemId", itemId);
        context.editedData.addProperty("Stock Level", original.get("stockLevel").getAsString());
        context.editedData.addProperty("Selling Price", original.get("sellingPrice").getAsString());
        context.tableName = "items";

        new EditDialog(null, updatedData -> {
            // Ensure poId is preserved from the original data
            String itemIdString = context.originalData.get("itemId").getAsString();
            updatedData.addProperty("itemId", itemIdString);

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
    
}
