// In SalesTableHandler.java

package com.assignment2.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap; // Added for itemsMap initialization in createSalesPage
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.assignment2.gui_albert.TablePage; // Your TablePage class
import com.assignment2.helpers.JsonStorageHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SalesTableHandler implements TableActionHandler {
    private JFrame currentPage; // This will be the TablePage instance
    // private TablePage page; // Redundant if currentPage is the TablePage

    private static final String SALES_FILE_PATH = "Sales.txt";
    private static final String ITEMS_FILE_PATH = "items.txt";
    private Map<String, JsonObject> itemsMap;

    // Constructor remains the same
    public SalesTableHandler(JFrame currentPage, TablePage page) {
        this.currentPage = currentPage; // This will be the salesDisplayPage
        // this.page = page; // if different from currentPage
        loadItemsData();
    }

    // Getter for itemsMap (if needed publicly, otherwise can be private/package-private)
    public Map<String, JsonObject> getItemsMap() {
        return this.itemsMap;
    }

    // Getter for raw sales data (if needed publicly)
    public JsonArray getRawSalesData() {
        try {
            return JsonStorageHelper.loadAsJsonArray(SALES_FILE_PATH);
        } catch (IOException e) {
            System.err.println(SALES_FILE_PATH + " not found or unreadable. Returning empty sales list. " + e.getMessage());
            if (this.currentPage != null) {
                JOptionPane.showMessageDialog(this.currentPage,
                        "Error loading sales data from " + SALES_FILE_PATH + ": " + e.getMessage() + "\nDisplaying empty list.",
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }
            return new JsonArray();
        } catch (IllegalStateException e) {
            System.err.println("Error parsing " + SALES_FILE_PATH + ". Is it a valid JSON array? Displaying empty list. " + e.getMessage());
            if (this.currentPage != null) {
                JOptionPane.showMessageDialog(this.currentPage, "Error parsing " + SALES_FILE_PATH + ". Is it a valid JSON array? Displaying empty list. Details: " + e.getMessage(), "JSON Error", JOptionPane.ERROR_MESSAGE);
            }
            return new JsonArray();
        }
    }

    // In SalesTableHandler.java
    public static TablePage createSalesPage() {
        System.out.println("SalesTableHandler: Creating Sales Page...");
        String pageTitle = "Daily Sales Entry";
        boolean allowEdit = false; // Or true, based on your needs for sales
        boolean allowDelete = false; // Or true
        boolean allowAdd = true; // Sales page needs add
        String pointerKeyPath = "saleId"; // Example, if you use it for anything
        JsonArray initialJsonData = new JsonArray(); // Start empty, will be loaded

        // If using the simpler existing constructor:
        // TablePage salesDisplayPage = new TablePage(pageTitle, allowEdit, allowDelete, allowAdd, pointerKeyPath, initialJsonData);

        // OR if using the more complex constructor:
        String[] excludedKeys = new String[0];
        Map<String, String> combinedColumns = new LinkedHashMap<>();
        // Define the column order to match what convertSalesDataForDisplay will produce:
        List<String> columnOrder = List.of("Sale ID", "Item Name", "Quantity Sold", "Total Sales", "Created At");
        boolean allowApproveReject = false;

        TablePage salesDisplayPage = new TablePage(
            pageTitle, allowEdit, allowDelete, allowAdd,
            excludedKeys, combinedColumns, columnOrder,
            pointerKeyPath, initialJsonData, allowApproveReject
        );

        // The rest of the setup remains the same:
        SalesTableHandler salesHandler = new SalesTableHandler(salesDisplayPage, salesDisplayPage);
        salesDisplayPage.setTableActionHandler(salesHandler);

        JsonArray rawSalesData = salesHandler.getRawSalesData();
        Map<String, JsonObject> itemsMapInstance = salesHandler.getItemsMap();
        JsonArray displayData = SalesTableHandler.convertSalesDataForDisplay(rawSalesData, itemsMapInstance);
        
        salesDisplayPage.refreshTableData(displayData);

        return salesDisplayPage;
    }


    // loadItemsData method (ensure it uses this.currentPage for dialogs if needed)
    private void loadItemsData() {
        this.itemsMap = new HashMap<>();
        try {
            JsonArray itemsArray = JsonStorageHelper.loadAsJsonArray(ITEMS_FILE_PATH);
            for (JsonElement itemElement : itemsArray) {
                JsonObject itemObject = itemElement.getAsJsonObject();
                String itemIdStr;
                JsonElement itemIdElement = itemObject.get("itemId");

                if (itemIdElement == null || itemIdElement.isJsonNull()) continue;

                if (itemIdElement.isJsonPrimitive() && itemIdElement.getAsJsonPrimitive().isString()) {
                    itemIdStr = itemIdElement.getAsString();
                } else if (itemIdElement.isJsonPrimitive() && itemIdElement.getAsJsonPrimitive().isNumber()) {
                    itemIdStr = itemIdElement.getAsNumber().toString();
                } else {
                    continue;
                }
                itemsMap.put(itemIdStr, itemObject);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Use this.currentPage for dialogs if it's set. 
            // In createSalesPage context, currentPage won't be set yet when loadItemsData is called by constructor.
            // Consider passing the JFrame to loadItemsData if dialogs are critical during initial load.
            // For now, system.err will have to do if currentPage is null here.
            if (this.currentPage != null) {
                JOptionPane.showMessageDialog(this.currentPage, "Error loading items data from " + ITEMS_FILE_PATH + ": " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("Error loading items data: " + e.getMessage() + " (SalesTableHandler constructor, currentPage is null at this point if called from static factory)");
            }
        } catch (IllegalStateException e) {
             e.printStackTrace();
             if (this.currentPage != null) {
                JOptionPane.showMessageDialog(this.currentPage, "Error parsing " + ITEMS_FILE_PATH + ". Is it a valid JSON array? Details: " + e.getMessage(), "JSON Error", JOptionPane.ERROR_MESSAGE);
             } else {
                System.err.println("Error parsing items data: " + e.getMessage() + " (SalesTableHandler constructor, currentPage is null at this point if called from static factory)");
             }
        }
    }

    @Override
    public void onAdd() {
        loadItemsData(); // Refresh items data, especially stock levels, before adding a sale

        if (itemsMap == null || itemsMap.isEmpty()) {
            JOptionPane.showMessageDialog(currentPage, "Cannot add sale: Items data could not be loaded or is empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> itemNamesForDropdown = new ArrayList<>();
        // This map helps retrieve the full item JsonObject from the selected itemName.
        Map<String, JsonObject> itemObjectsByName = new HashMap<>();

        for (JsonObject item : itemsMap.values()) {
            if (item.has("itemName") && !item.get("itemName").isJsonNull()) {
                String name = item.get("itemName").getAsString();
                // Simple handling for duplicate names: first one wins for dropdown.
                // A more robust solution might append itemId or use a more complex selection UI.
                if (!itemObjectsByName.containsKey(name)) {
                    itemNamesForDropdown.add(name);
                    itemObjectsByName.put(name, item);
                }
            }
        }

        if (itemNamesForDropdown.isEmpty()) {
            JOptionPane.showMessageDialog(currentPage, "No items available to sell (or items in " + ITEMS_FILE_PATH + " lack 'itemName').", "Setup Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 1. Select Item (Dropdown)
        String selectedItemName = (String) JOptionPane.showInputDialog(
                currentPage,
                "Select Item:",
                "Add Sale - Step 1 of 2",
                JOptionPane.PLAIN_MESSAGE,
                null, // Icon
                itemNamesForDropdown.toArray(new String[0]), // Options
                itemNamesForDropdown.get(0)); // Default selection

        if (selectedItemName == null || selectedItemName.trim().isEmpty()) {
            return; // User cancelled
        }

        // 2. Input Quantity
        String quantitySoldStr = JOptionPane.showInputDialog(currentPage, "Enter quantity sold for " + selectedItemName + ":", "Add Sale - Step 2 of 2", JOptionPane.PLAIN_MESSAGE);
        if (quantitySoldStr == null || quantitySoldStr.trim().isEmpty()) {
            return; // User cancelled or entered nothing
        }

        // --- Data Validation and Processing ---
        int quantitySold;
        try {
            quantitySold = Integer.parseInt(quantitySoldStr.trim());
            if (quantitySold <= 0) {
                JOptionPane.showMessageDialog(currentPage, "Quantity sold must be a positive whole number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(currentPage, "Invalid quantity format. Please enter a whole number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JsonObject selectedItemObject = itemObjectsByName.get(selectedItemName);
        // This check is mostly a safeguard; selectedItemName should always be in itemObjectsByName.
        if (selectedItemObject == null) {
            JOptionPane.showMessageDialog(currentPage, "Selected item '" + selectedItemName + "' details not found. Data might be inconsistent.", "Internal Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String itemId = selectedItemObject.get("itemId").getAsString(); // itemId was standardized to string during loadItemsData
        double sellingPrice = selectedItemObject.get("sellingPrice").getAsDouble();
        int currentStockLevel = selectedItemObject.get("stockLevel").getAsInt(); // Assuming 'stockLevel' in items.txt is parsable to int

        // Validate stock level
        if (quantitySold > currentStockLevel) {
            JOptionPane.showMessageDialog(currentPage, "Quantity sold (" + quantitySold + ") exceeds available stock (" + currentStockLevel + ") for " + selectedItemName + ".", "Stock Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- Calculations ---
        double totalSales = quantitySold * sellingPrice;
        int newStockLevel = currentStockLevel - quantitySold;

        // --- Generate New Sale ID ---
        String newSaleId = generateNewSaleId();

        // --- Create Sale JsonObject ---
        JsonObject newSale = new JsonObject();
        newSale.addProperty("saleId", newSaleId);
        newSale.addProperty("itemId", itemId);
        newSale.addProperty("quantitySold", String.valueOf(quantitySold)); // Store as string as per Sales.txt format
        newSale.addProperty("totalSales", String.format("%.2f", totalSales)); // Store as string, formatted
        newSale.addProperty("createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // --- Update Stock Level in items.txt (Requirement 5) ---
        boolean stockUpdated = updateItemStockInFile(itemId, newStockLevel);
        if (!stockUpdated) {
            // updateItemStockInFile method would have shown an error message.
            // Sale is not added if stock cannot be updated.
            return;
        }

        // --- Add Sale to Sales.txt ---
        try {
            JsonArray salesArray;
            try { // Load existing sales or create new array if file doesn't exist/is empty
                salesArray = JsonStorageHelper.loadAsJsonArray(SALES_FILE_PATH);
            } catch (IOException e) {
                salesArray = new JsonArray(); // Start with a new array if file not found or error
            }
            salesArray.add(newSale);
            JsonStorageHelper.saveJsonArray("Sales.txt", salesArray); // Overwrites the file
            
            JOptionPane.showMessageDialog(currentPage, "Sale added successfully! New Sale ID: " + newSaleId, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(currentPage, "Error saving sale to " + SALES_FILE_PATH + ": " + e.getMessage(), "File I/O Error", JOptionPane.ERROR_MESSAGE);
            // At this point, stock might have been deducted but sale not saved.
            // A robust system would implement a rollback for stock, but that's complex.
            // For now, we notify the user.
        }

        // --- Refresh Table View ---
        JsonArray updatedSalesList = getLatestSalesData();
        if (currentPage instanceof TablePage) {
             ((TablePage) currentPage).refreshTableData(convertSalesDataForDisplay(updatedSalesList, this.itemsMap));
        } else {
            System.err.println("Warning: currentPage is not an instance of TablePage. Cannot refresh table view.");
        }
    }

    private String generateNewSaleId() {
        int maxId = 0;
        boolean foundAnyIds = false;
        JsonArray salesArray;
        try {
            salesArray = JsonStorageHelper.loadAsJsonArray(SALES_FILE_PATH);
        } catch (IOException e) { // File might not exist or is not a valid JSON array
            System.out.println(SALES_FILE_PATH + " not found or is empty/invalid. Starting Sale ID from 601.");
            return "601"; // Default start ID as per example data
        }

        for (JsonElement saleElement : salesArray) {
            JsonObject saleObject = saleElement.getAsJsonObject();
            if (saleObject.has("saleId") && !saleObject.get("saleId").isJsonNull()) {
                try {
                    int currentId = Integer.parseInt(saleObject.get("saleId").getAsString());
                    if (currentId > maxId) {
                        maxId = currentId;
                    }
                    foundAnyIds = true;
                } catch (NumberFormatException ex) {
                    System.err.println("Warning: Non-integer or unparseable saleId found in " + SALES_FILE_PATH + ": " + saleObject.get("saleId").getAsString());
                }
            }
        }

        if (!foundAnyIds) { // File existed but was empty or contained no parseable saleIds
            return "601"; // Default start ID
        }
        return String.valueOf(maxId + 1);
    }
    
    private boolean updateItemStockInFile(String itemIdToUpdate, int newStockLevel) {
        try {
            JsonArray itemsArray = JsonStorageHelper.loadAsJsonArray(ITEMS_FILE_PATH);
            JsonArray updatedItemsArray = new JsonArray();
            boolean itemFoundAndUpdated = false;

            for (JsonElement itemElement : itemsArray) {
                JsonObject itemObject = itemElement.getAsJsonObject();
                String currentItemIdStr = ""; // Initialize to avoid issues if itemId is missing/null

                JsonElement itemIdJsonElement = itemObject.get("itemId");
                 if (itemIdJsonElement != null && !itemIdJsonElement.isJsonNull()) {
                    if(itemIdJsonElement.isJsonPrimitive() && itemIdJsonElement.getAsJsonPrimitive().isString()){
                        currentItemIdStr = itemIdJsonElement.getAsString();
                    } else if (itemIdJsonElement.isJsonPrimitive() && itemIdJsonElement.getAsJsonPrimitive().isNumber()){
                        currentItemIdStr = itemIdJsonElement.getAsNumber().toString();
                    }
                 }

                if (currentItemIdStr.equals(itemIdToUpdate)) {
                    // 'stockLevel' in items.txt is given as string, so save as string
                    itemObject.addProperty("stockLevel", String.valueOf(newStockLevel)); 
                    itemFoundAndUpdated = true;
                }
                updatedItemsArray.add(itemObject);
            }

            if (itemFoundAndUpdated) {
                JsonStorageHelper.saveJsonArray(ITEMS_FILE_PATH, updatedItemsArray); // Overwrite items.txt
                loadItemsData(); // Important: Refresh the local itemsMap cache as stock has changed
                return true;
            } else {
                // This case should ideally not be reached if item selection logic is correct
                System.err.println("Critical Error: Item ID '" + itemIdToUpdate + "' not found in " + ITEMS_FILE_PATH + " during stock update attempt.");
                JOptionPane.showMessageDialog(currentPage, "Critical Error: Item to update stock for (ID: " + itemIdToUpdate + ") was not found in " + ITEMS_FILE_PATH + ". Sale process aborted before saving sale details.", "Data Consistency Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(currentPage, "Error updating stock in " + ITEMS_FILE_PATH + ": " + e.getMessage(), "File I/O Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    @Override
    public void onEdit(JsonObject record) {
        throw new UnsupportedOperationException("onEdit is not supported for Sales records via this handler.");
    }

    @Override
    public void onDelete(JsonObject rowData, String pointerKeyPath) {
        throw new UnsupportedOperationException("onDelete is not supported for Sales records via this handler.");
    }

    // Renamed from 'convert' to be more descriptive and static.
    // Takes itemsMap for efficiency, avoiding repeated file reads.
    public static JsonArray convertSalesDataForDisplay(JsonArray rawSalesArray, Map<String, JsonObject> itemsMapForConversion) {
        JsonArray convertedArray = new JsonArray();

        for (JsonElement saleEl : rawSalesArray) {
            JsonObject originalSale = saleEl.getAsJsonObject();
            JsonObject convertedSale = new JsonObject(); // This will be the object for the table row

            convertedSale.addProperty("Sale ID", originalSale.get("saleId").getAsString());

            String itemId = originalSale.get("itemId").getAsString();
            String itemName = "Item Not Found"; // Default if lookup fails
            if (itemsMapForConversion != null && itemsMapForConversion.containsKey(itemId)) {
                JsonObject item = itemsMapForConversion.get(itemId);
                if(item.has("itemName") && !item.get("itemName").isJsonNull()){
                    itemName = item.get("itemName").getAsString();
                } else {
                    itemName = "Item Name Missing";
                }
            } else {
                 // Fallback: if itemsMap is null or item not in map (e.g. item deleted after sale)
                 // This is less efficient as it might read the items file multiple times if map is not good.
                System.err.println("Item ID " + itemId + " not found in provided itemsMap. Attempting static lookup for display.");
                itemName = getNameByIdStatic(ITEMS_FILE_PATH, "itemId", itemId, "itemName");
            }
            convertedSale.addProperty("Item Name", itemName);
            
            // Ensure "quantitySold" exists before trying to access it.
            if (originalSale.has("quantitySold") && !originalSale.get("quantitySold").isJsonNull()) {
                 convertedSale.addProperty("Quantity Sold", originalSale.get("quantitySold").getAsString());
            } else {
                convertedSale.addProperty("Quantity Sold", "N/A");
            }

            if (originalSale.has("totalSales") && !originalSale.get("totalSales").isJsonNull()) {
                convertedSale.addProperty("Total Sales", originalSale.get("totalSales").getAsString());
            } else {
                convertedSale.addProperty("Total Sales", "N/A");
            }
            
            if (originalSale.has("createdAt") && !originalSale.get("createdAt").isJsonNull()) {
                String createdAtFull = originalSale.get("createdAt").getAsString();
                convertedSale.addProperty("Created At", createdAtFull.split(" ")[0]); // Date part only
            } else {
                convertedSale.addProperty("Created At", "N/A");
            }
            convertedArray.add(convertedSale);
        }
        return convertedArray;
    }

    // Static helper for item name lookup, used as a fallback by convertSalesDataForDisplay.
    private static String getNameByIdStatic(String filePath, String idKeyToMatch, String targetIdValue, String nameKeyToFetch) {
        try {
            JsonArray itemsArray = JsonStorageHelper.loadAsJsonArray(filePath);
            for (JsonElement el : itemsArray) {
                JsonObject itemObj = el.getAsJsonObject();
                JsonElement idElement = itemObj.get(idKeyToMatch);

                if (idElement == null || idElement.isJsonNull()) continue;

                String currentItemIdStr;
                if (idElement.isJsonPrimitive() && idElement.getAsJsonPrimitive().isString()) {
                    currentItemIdStr = idElement.getAsString();
                } else if (idElement.isJsonPrimitive() && idElement.getAsJsonPrimitive().isNumber()) {
                    currentItemIdStr = idElement.getAsNumber().toString();
                } else {
                    continue; // Skip if itemId is not a string or number
                }

                if (currentItemIdStr.equals(targetIdValue)) {
                    if (itemObj.has(nameKeyToFetch) && !itemObj.get(nameKeyToFetch).isJsonNull()){
                        return itemObj.get(nameKeyToFetch).getAsString();
                    } else {
                        return "Name N/A"; // Item found, but name field missing
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in getNameByIdStatic looking for ID '" + targetIdValue + "' in '" + filePath + "': " + e.getMessage());
            // e.printStackTrace(); // Uncomment for detailed debug
        }
        return "Unknown Item"; // Default if not found or error during lookup
    }

    // Renamed from getLatestData to be more specific
    private JsonArray getLatestSalesData() {
        try {
            return JsonStorageHelper.loadAsJsonArray(SALES_FILE_PATH);
        } catch (IOException e) { // Includes FileNotFoundException
            // If sales file doesn't exist, it's like an empty list of sales
            System.out.println(SALES_FILE_PATH + " not found or unreadable. Returning empty sales list. " + e.getMessage());
            return new JsonArray();
        } catch (IllegalStateException e) { // GSON parsing error
            JOptionPane.showMessageDialog(currentPage, "Error parsing " + SALES_FILE_PATH + ". Is it a valid JSON array? Displaying empty list. Details: " + e.getMessage(), "JSON Error", JOptionPane.ERROR_MESSAGE);
            return new JsonArray();
        }
    }
}