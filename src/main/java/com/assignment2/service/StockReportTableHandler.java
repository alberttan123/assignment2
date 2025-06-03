package com.assignment2.service;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFrame;

import com.assignment2.gui_albert.TablePage;
import com.assignment2.helpers.JsonStorageHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class StockReportTableHandler implements TableActionHandler{
    private JFrame currentPage;
    private TablePage page;
    private String filePath = "items.txt";
    private Map<String, JsonObject> originalDataMap = new LinkedHashMap<>();
    
    public StockReportTableHandler(JFrame currentPage, TablePage page) {
        this.currentPage = currentPage;
        this.page = page;
    }

    @Override
    public void onAdd(){
        throw new UnsupportedOperationException("onAdd unused");
    }

    @Override
    public void onEdit(JsonObject record){
        throw new UnsupportedOperationException("onEdit unused");
    }    

    @Override
    public void onDelete(JsonObject rowData, String pointerKeyPath){
        throw new UnsupportedOperationException("onEdit unused");
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
            converted.addProperty("Quantity", getNameById("items.txt", "itemId", original.get("itemId").getAsInt(), "stockLevel"));
            
            try {
                // Setting the value for the Status of the Quantity
                if (original.get("stockLevel").getAsInt() < 50) {
                    converted.addProperty("Status", "Critical");               
                } else if (original.get("stockLevel").getAsInt() < 100) {
                    converted.addProperty("Status", "Low");
                } else {
                    converted.addProperty("Status", "Normal");                
                }
            } catch (Exception e) {
                // Setting a default value
                converted.addProperty("Status", "N/A");
                System.err.println("Error determining stock level status: " + e.getMessage());
            }

            
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
