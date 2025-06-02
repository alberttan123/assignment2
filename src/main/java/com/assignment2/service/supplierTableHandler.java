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

public class SupplierTableHandler implements TableActionHandler{
    private JFrame currentPage;
    private TablePage page;
    private String filePath = "Supplier.txt";
    private Map<String, JsonObject> originalDataMap = new LinkedHashMap<>();

    public SupplierTableHandler(JFrame currentPage, TablePage page) {
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
        // Extract the item name from the input JSON record (table header name)
        String supplierName = record.get("Supplier").getAsString();

        // Lookup the itemId using itemName from the file named items.txt
        String supplierId = JsonStorageHelper.lookupValueByLabel("Supplier.txt", "name", "supplierId", supplierName);
        

        JsonArray itemList;
        try {
            // Load the current list of items from filepath
            itemList = JsonStorageHelper.loadAsJsonArray(filePath);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to load Supplier file.");
            return;
        }

        // Iterate through the loaded list to find the original record using suupplierId
        JsonObject original = null;
        for (JsonElement el : itemList) {
            JsonObject obj = el.getAsJsonObject();
            String id = obj.get("supplierId").getAsString();
            originalDataMap.put(id, obj);

            // If supplierId matches, mark it as the original record
            original = obj;
        }

        // If no matching original record was found, show an error
        if (original == null) {
            JOptionPane.showMessageDialog(null, "Original record not found.");
            return;
        }

        Map<String, FieldDefinition> fieldDefs = new LinkedHashMap<>();
        // Field for Supplier Name
        fieldDefs.put("supplierId", FieldDefinition
            .dropdown("Supplier.txt", "name", "supplierId")
            .withLabel("Supplier Name")
            .withKey("supplierId")
            .required());

        // Field for Address
        fieldDefs.put("Address", FieldDefinition
            .of("String")
            .withLabel("Supplier Address")
            .withKey("address")
            .required());

        // Set up the edit context with original and editable data
        EditDialogContext context = new EditDialogContext();
        context.originalData = original.deepCopy();
        context.editedData = new JsonObject();

        // Pre-fill the editable fields with original data
        context.editedData.addProperty("supplierId", supplierId);
        context.editedData.addProperty("Address", original.get("address").getAsString());
        context.tableName = "supplier_name";

        new EditDialog(null, updatedData -> {
            // Ensure supplierId is preserved from the original data
            String itemIdString = context.originalData.get("supplierId").getAsString();
            updatedData.addProperty("supplierID", itemIdString);

            try {
                // Update the Supplier.txt file with the new data
                JsonStorageHelper.updateOrInsert("Supplier.txt", updatedData, "supplierId");

                // Reload updated data and refresh the UI table
                JsonArray updatedList = JsonStorageHelper.loadAsJsonArray("Supplier.txt");
                page.refreshTableData(convert(updatedList));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed to update Supplier record.");
                e.printStackTrace();
            }
        }, fieldDefs, context).setVisible(true); // Show the dialog
    }

    @Override
    public void onDelete(JsonObject rowData, String pointerKeyPath){
        // Extract the value of the supplierId from the row that is being deleted
        String keyVal = rowData.get("supplierId").getAsString();
        // Call helper method to remove the supplier from the JSON file based on supplierId
        deleteRowFromJson(keyVal, pointerKeyPath);
        System.out.println("Deleted item.");
        page.refreshTableData(getLatestData());
    }

    // Method to load latest data from items.txt and return it as JsonArray
    private JsonArray getLatestData(){
        String jsonFilePath = "Supplier.txt";
        JsonArray root = null;
        try {
            // Load the entire file as a JsonArray
            root = JsonStorageHelper.loadAsJsonArray(jsonFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;// Return the loaded data
    }

    public static JsonArray convert(JsonArray rawArray){
        // Initialize a new array to hold the converted objects
        JsonArray convertedArray = new JsonArray();

        for (JsonElement el : rawArray) {
            JsonObject original = el.getAsJsonObject(); // Original JSON object
            JsonObject converted = new JsonObject();    // New object to store converted data
            System.out.println(original);

            // Lookup and add item details based on itemId
            converted.addProperty("supplierId", original.get("supplierId").getAsInt());
            converted.addProperty("Supplier", getNameById("Supplier.txt", "supplierId", original.get("supplierId").getAsInt(), "name"));
            converted.addProperty("Address", getNameById("Supplier.txt", "supplierId", original.get("supplierId").getAsInt(), "address"));

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
    
    // Delete a row from a JSON array using keyValue
    private void deleteRowFromJson(String keyValue, String pointerKeyPath) {
        try {
            JsonArray arr = getLatestData(); // e.g., your array of items

            for (int i = 0; i < arr.size(); i++) {
                JsonObject obj = arr.get(i).getAsJsonObject();

                // Retrieve the value of the field based on the pointerKeyPath
                JsonElement element = obj.get(pointerKeyPath);
                if (element == null) continue;

                String value = element.getAsString();
                if (value.equals(keyValue)) {
                    arr.remove(i); // Remove the object at index i

                    // Log the file path and the updated content
                    System.out.println("Deleting item with " + pointerKeyPath + " = " + keyValue);
                    System.out.println("Saving changes to file: " + "Supplier.txt");

                    JsonStorageHelper.saveToJson("Supplier.txt", arr); // Save updated data

                    System.out.println("Data successfully saved to " + "Supplier.txt");
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
