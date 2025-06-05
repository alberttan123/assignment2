// In SalesTableHandler.java

package com.assignment2.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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

    public SalesTableHandler(JFrame currentPage, TablePage page) {
        this.currentPage = currentPage;
        loadItemsData();
    }

    public Map<String, JsonObject> getItemsMap() {
        return this.itemsMap;
    }

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

    public static TablePage createSalesPage() {
        System.out.println("SalesTableHandler: Creating Sales Page...");
        String pageTitle = "Daily Sales Entry";
        boolean allowEdit = true; // MODIFIED: Enable editing
        boolean allowDelete = true; // MODIFIED: Enable deleting
        boolean allowAdd = true;
        String pointerKeyPath = "saleId"; // Used by TablePage to identify original data's key
        JsonArray initialJsonData = new JsonArray();

        String[] excludedKeys = new String[0];
        Map<String, String> combinedColumns = new LinkedHashMap<>();
        List<String> columnOrder = List.of("Sale ID", "Item Name", "Quantity Sold", "Total Sales", "Created At");
        boolean allowApproveReject = false;

        TablePage salesDisplayPage = new TablePage(
            pageTitle, allowEdit, allowDelete, allowAdd,
            excludedKeys, combinedColumns, columnOrder,
            pointerKeyPath, initialJsonData, allowApproveReject
        );

        SalesTableHandler salesHandler = new SalesTableHandler(salesDisplayPage, salesDisplayPage);
        salesDisplayPage.setTableActionHandler(salesHandler);

        JsonArray rawSalesData = salesHandler.getRawSalesData();
        Map<String, JsonObject> itemsMapInstance = salesHandler.getItemsMap(); // Get the loaded map
        JsonArray displayData = SalesTableHandler.convertSalesDataForDisplay(rawSalesData, itemsMapInstance);
        
        salesDisplayPage.refreshTableData(displayData);

        return salesDisplayPage;
    }

    // Helper to safely get itemId as String
    private String getItemIdAsString(JsonObject jsonObject, String keyName) {
        JsonElement idElement = jsonObject.get(keyName);
        if (idElement == null || idElement.isJsonNull()) return null;

        if (idElement.isJsonPrimitive() && idElement.getAsJsonPrimitive().isString()) {
            return idElement.getAsString();
        } else if (idElement.isJsonPrimitive() && idElement.getAsJsonPrimitive().isNumber()) {
            return idElement.getAsNumber().toString();
        }
        return null; // Or throw an error if type is unexpected
    }

    private void loadItemsData() {
        this.itemsMap = new HashMap<>();
        try {
            JsonArray itemsArray = JsonStorageHelper.loadAsJsonArray(ITEMS_FILE_PATH);
            for (JsonElement itemElement : itemsArray) {
                JsonObject itemObject = itemElement.getAsJsonObject();
                String itemIdStr = getItemIdAsString(itemObject, "itemId");

                if (itemIdStr != null) {
                    itemsMap.put(itemIdStr, itemObject);
                } else {
                    System.err.println("Warning: Item found with missing or invalid itemId in " + ITEMS_FILE_PATH);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (this.currentPage != null) {
                JOptionPane.showMessageDialog(this.currentPage, "Error loading items data from " + ITEMS_FILE_PATH + ": " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("Error loading items data: " + e.getMessage() + " (SalesTableHandler constructor, currentPage is null if called from static factory and error occurs here)");
            }
        } catch (IllegalStateException e) {
             e.printStackTrace();
             if (this.currentPage != null) {
                JOptionPane.showMessageDialog(this.currentPage, "Error parsing " + ITEMS_FILE_PATH + ". Is it a valid JSON array? Details: " + e.getMessage(), "JSON Error", JOptionPane.ERROR_MESSAGE);
             } else {
                System.err.println("Error parsing items data: " + e.getMessage() + " (SalesTableHandler constructor, currentPage is null if called from static factory and error occurs here)");
             }
        }
    }

    @Override
    public void onAdd() {
        loadItemsData(); 

        if (itemsMap == null || itemsMap.isEmpty()) {
            JOptionPane.showMessageDialog(currentPage, "Cannot add sale: Items data could not be loaded or is empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> itemNamesForDropdown = new ArrayList<>();
        Map<String, JsonObject> itemObjectsByName = new HashMap<>();

        for (JsonObject item : itemsMap.values()) {
            if (item.has("itemName") && !item.get("itemName").isJsonNull()) {
                String name = item.get("itemName").getAsString();
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

        String selectedItemName = (String) JOptionPane.showInputDialog(
                currentPage, "Select Item:", "Add Sale - Step 1 of 2",
                JOptionPane.PLAIN_MESSAGE, null, itemNamesForDropdown.toArray(new String[0]), itemNamesForDropdown.get(0));

        if (selectedItemName == null || selectedItemName.trim().isEmpty()) return;

        String quantitySoldStr = JOptionPane.showInputDialog(currentPage, "Enter quantity sold for " + selectedItemName + ":", "Add Sale - Step 2 of 2", JOptionPane.PLAIN_MESSAGE);
        if (quantitySoldStr == null || quantitySoldStr.trim().isEmpty()) return;

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
        if (selectedItemObject == null) {
            JOptionPane.showMessageDialog(currentPage, "Selected item '" + selectedItemName + "' details not found.", "Internal Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String itemId = getItemIdAsString(selectedItemObject, "itemId");
        if (itemId == null) { // Should not happen if item was in map
             JOptionPane.showMessageDialog(currentPage, "Selected item '" + selectedItemName + "' has invalid Item ID.", "Internal Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double sellingPrice = selectedItemObject.get("sellingPrice").getAsDouble();
        int currentStockLevel = selectedItemObject.get("stockLevel").getAsInt();

        if (quantitySold > currentStockLevel) {
            JOptionPane.showMessageDialog(currentPage, "Quantity sold (" + quantitySold + ") exceeds available stock (" + currentStockLevel + ") for " + selectedItemName + ".", "Stock Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double totalSales = quantitySold * sellingPrice;
        int newStockLevel = currentStockLevel - quantitySold;
        String newSaleId = generateNewSaleId();

        JsonObject newSale = new JsonObject();
        newSale.addProperty("saleId", newSaleId);
        newSale.addProperty("itemId", itemId);
        newSale.addProperty("quantitySold", String.valueOf(quantitySold));
        newSale.addProperty("totalSales", String.format("%.2f", totalSales));
        newSale.addProperty("createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        boolean stockUpdated = updateItemStockInFile(itemId, newStockLevel);
        if (!stockUpdated) return;

        try {
            JsonArray salesArray = getLatestSalesData(); // Handles file not existing by returning empty array
            salesArray.add(newSale);
            JsonStorageHelper.saveToJson(SALES_FILE_PATH, salesArray);
            JOptionPane.showMessageDialog(currentPage, "Sale added successfully! New Sale ID: " + newSaleId, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(currentPage, "Error saving sale to " + SALES_FILE_PATH + ": " + e.getMessage(), "File I/O Error", JOptionPane.ERROR_MESSAGE);
        }
        refreshSalesTable();
    }

    private String generateNewSaleId() {
        int maxId = 0;
        boolean foundAnyIds = false;
        JsonArray salesArray = getLatestSalesData(); // Handles file not existing

        for (JsonElement saleElement : salesArray) {
            JsonObject saleObject = saleElement.getAsJsonObject();
            if (saleObject.has("saleId") && !saleObject.get("saleId").isJsonNull()) {
                try {
                    int currentId = Integer.parseInt(saleObject.get("saleId").getAsString());
                    if (currentId > maxId) maxId = currentId;
                    foundAnyIds = true;
                } catch (NumberFormatException ex) {
                    System.err.println("Warning: Non-integer saleId found: " + saleObject.get("saleId").getAsString());
                }
            }
        }
        return String.valueOf(foundAnyIds ? maxId + 1 : 601);
    }
    
    private boolean updateItemStockInFile(String itemIdToUpdate, int newStockLevel) {
        try {
            JsonArray itemsArray = JsonStorageHelper.loadAsJsonArray(ITEMS_FILE_PATH);
            JsonArray updatedItemsArray = new JsonArray();
            boolean itemFoundAndUpdated = false;

            for (JsonElement itemElement : itemsArray) {
                JsonObject itemObject = itemElement.getAsJsonObject();
                String currentItemIdStr = getItemIdAsString(itemObject, "itemId");

                if (currentItemIdStr != null && currentItemIdStr.equals(itemIdToUpdate)) {
                    itemObject.addProperty("stockLevel", String.valueOf(newStockLevel)); 
                    itemFoundAndUpdated = true;
                }
                updatedItemsArray.add(itemObject);
            }

            if (itemFoundAndUpdated) {
                JsonStorageHelper.saveToJson(itemIdToUpdate, itemIdToUpdate);
                loadItemsData(); // Refresh local itemsMap as stock has changed
                return true;
            } else {
                System.err.println("Critical Error: Item ID '" + itemIdToUpdate + "' not found in " + ITEMS_FILE_PATH + " during stock update.");
                JOptionPane.showMessageDialog(currentPage, "Critical Error: Item ID " + itemIdToUpdate + " not found for stock update.", "Data Consistency Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(currentPage, "Error updating stock in " + ITEMS_FILE_PATH + ": " + e.getMessage(), "File I/O Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Helper to find the original sale object from Sales.txt
    private JsonObject findOriginalSaleById(String saleId) throws IOException {
        JsonArray salesArray = JsonStorageHelper.loadAsJsonArray(SALES_FILE_PATH);
        for (JsonElement saleElement : salesArray) {
            JsonObject saleObject = saleElement.getAsJsonObject();
            if (saleObject.has("saleId") && saleObject.get("saleId").getAsString().equals(saleId)) {
                return saleObject;
            }
        }
        return null; // Not found
    }
    
    @Override
    public void onEdit(JsonObject displayRowData) {
        String saleIdToEdit = displayRowData.get("Sale ID").getAsString();
        String currentQuantitySoldDisplay = displayRowData.get("Quantity Sold").getAsString();

        try {
            // 1. Get original sale details from Sales.txt
            JsonObject originalSale = findOriginalSaleById(saleIdToEdit);
            if (originalSale == null) {
                JOptionPane.showMessageDialog(currentPage, "Sale ID " + saleIdToEdit + " not found. Cannot edit.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String itemId = originalSale.get("itemId").getAsString();
            int originalQuantitySoldFromFile = Integer.parseInt(originalSale.get("quantitySold").getAsString());

            // 2. Get item details (price, current stock from items.txt)
            loadItemsData(); // Ensure itemsMap is fresh for stock levels
            JsonObject itemDetails = itemsMap.get(itemId);
            if (itemDetails == null) {
                JOptionPane.showMessageDialog(currentPage, "Item details for ID " + itemId + " (Sale " + saleIdToEdit + ") not found. Cannot edit.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String itemName = itemDetails.has("itemName") ? itemDetails.get("itemName").getAsString() : "Unknown Item";
            double sellingPrice = itemDetails.get("sellingPrice").getAsDouble();
            int currentStockLevelInFile = Integer.parseInt(itemDetails.get("stockLevel").getAsString()); // Stock in items.txt

            // 3. Prompt for new quantity
            String newQuantityStr = (String) JOptionPane.showInputDialog(
                    currentPage,
                    "Editing Sale for: " + itemName + "\nOriginal Quantity: " + originalQuantitySoldFromFile + "\nEnter new quantity sold:",
                    "Edit Sale ID: " + saleIdToEdit,
                    JOptionPane.PLAIN_MESSAGE, null, null, currentQuantitySoldDisplay);

            if (newQuantityStr == null || newQuantityStr.trim().isEmpty()) return; // User cancelled

            int newQuantitySold;
            try {
                newQuantitySold = Integer.parseInt(newQuantityStr.trim());
                if (newQuantitySold <= 0) {
                    JOptionPane.showMessageDialog(currentPage, "New quantity must be a positive whole number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(currentPage, "Invalid quantity format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 4. Calculate stock adjustments
            // Stock of the item *before* this particular sale was originally made:
            int stockAvailableBeforeThisSale = currentStockLevelInFile + originalQuantitySoldFromFile;

            if (newQuantitySold > stockAvailableBeforeThisSale) {
                JOptionPane.showMessageDialog(currentPage,
                        "New quantity (" + newQuantitySold + ") exceeds total available stock (" + stockAvailableBeforeThisSale + ") for " + itemName + " (current stock " + currentStockLevelInFile + " + originally sold " + originalQuantitySoldFromFile + ").",
                        "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // The change in quantity sold compared to the original sale quantity
            int quantityChangeDelta = newQuantitySold - originalQuantitySoldFromFile;
            // New stock level to be written to items.txt: current stock - changeDelta
            int finalNewStockLevelForItem = currentStockLevelInFile - quantityChangeDelta;

            // 5. Recalculate total sales
            double newTotalSales = newQuantitySold * sellingPrice;

            // 6. Update Sales.txt
            JsonArray salesArray = JsonStorageHelper.loadAsJsonArray(SALES_FILE_PATH);
            JsonArray updatedSalesArray = new JsonArray();
            boolean saleUpdatedInFile = false;
            for (JsonElement saleElement : salesArray) {
                JsonObject currentSale = saleElement.getAsJsonObject();
                if (currentSale.get("saleId").getAsString().equals(saleIdToEdit)) {
                    currentSale.addProperty("quantitySold", String.valueOf(newQuantitySold));
                    currentSale.addProperty("totalSales", String.format("%.2f", newTotalSales));
                    // Optional: currentSale.addProperty("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    saleUpdatedInFile = true;
                }
                updatedSalesArray.add(currentSale);
            }
            if (!saleUpdatedInFile) { // Should ideally not happen
                JOptionPane.showMessageDialog(currentPage, "Failed to find Sale ID " + saleIdToEdit + " in Sales.txt during update. Aborting.", "Internal Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JsonStorageHelper.saveToJson(SALES_FILE_PATH, updatedSalesArray);

            // 7. Update stock level in items.txt
            boolean stockFileUpdated = updateItemStockInFile(itemId, finalNewStockLevelForItem);
            if (!stockFileUpdated) {
                JOptionPane.showMessageDialog(currentPage, "Sale details updated, but FAILED to update stock in items.txt. Data may be inconsistent.", "Critical Stock Update Error", JOptionPane.ERROR_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(currentPage, "Sale ID " + saleIdToEdit + " updated successfully. Stock adjusted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            
            refreshSalesTable();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(currentPage, "Error during edit process: " + e.getMessage(), "File I/O Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (NumberFormatException | IllegalStateException e) { // Catching GSON parse errors for numbers/json
            JOptionPane.showMessageDialog(currentPage, "Error parsing data (e.g., quantity, price, stock): " + e.getMessage(), "Data Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    @Override
    public void onDelete(JsonObject displayRowData, String pointerKeyPath) { // pointerKeyPath is "saleId" from TablePage config
        String saleIdToDelete = displayRowData.get("Sale ID").getAsString(); // Get from displayed row

        int confirmation = JOptionPane.showConfirmDialog(
                currentPage,
                "Are you sure you want to delete Sale ID: " + saleIdToDelete + "?\nThis will add the sold quantity back to item stock.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) return;

        try {
            // 1. Get original sale details
            JsonObject originalSale = findOriginalSaleById(saleIdToDelete);
            if (originalSale == null) {
                JOptionPane.showMessageDialog(currentPage, "Sale ID " + saleIdToDelete + " not found. Cannot delete.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String itemId = originalSale.get("itemId").getAsString();
            int quantitySoldInDeletedSale = Integer.parseInt(originalSale.get("quantitySold").getAsString());

            // 2. Delete sale from Sales.txt
            JsonArray salesArray = JsonStorageHelper.loadAsJsonArray(SALES_FILE_PATH);
            JsonArray updatedSalesArray = new JsonArray();
            boolean saleRemoved = false;
            for (JsonElement saleElement : salesArray) {
                JsonObject currentSale = saleElement.getAsJsonObject();
                if (currentSale.get("saleId").getAsString().equals(saleIdToDelete)) {
                    saleRemoved = true;
                } else {
                    updatedSalesArray.add(currentSale);
                }
            }

            if (!saleRemoved) { // Should not happen if findOriginalSaleById found it
                JOptionPane.showMessageDialog(currentPage, "Internal Error: Sale ID " + saleIdToDelete + " not found for deletion after initial check.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JsonStorageHelper.saveToJson(SALES_FILE_PATH, updatedSalesArray);

            // 3. Revert stock in items.txt
            loadItemsData(); // Ensure itemsMap is fresh before getting current stock
            JsonObject itemToUpdate = itemsMap.get(itemId);
            if (itemToUpdate != null) {
                int currentStock = Integer.parseInt(itemToUpdate.get("stockLevel").getAsString());
                int newStockAfterReverting = currentStock + quantitySoldInDeletedSale;
                boolean stockReverted = updateItemStockInFile(itemId, newStockAfterReverting);
                 if (!stockReverted) {
                    JOptionPane.showMessageDialog(currentPage, "Sale record deleted, but FAILED to revert stock for item ID " + itemId + ". Data may be inconsistent.", "Critical Stock Update Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(currentPage, "Sale ID " + saleIdToDelete + " deleted. Stock reverted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                 JOptionPane.showMessageDialog(currentPage, "Sale ID " + saleIdToDelete + " deleted, but item ID " + itemId + " not found in items.txt. Stock NOT reverted.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            refreshSalesTable();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(currentPage, "Error during deletion: " + e.getMessage(), "File I/O Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (NumberFormatException | IllegalStateException e) {
            JOptionPane.showMessageDialog(currentPage, "Error parsing quantity/stock: " + e.getMessage(), "Data Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static JsonArray convertSalesDataForDisplay(JsonArray rawSalesArray, Map<String, JsonObject> itemsMapForConversion) {
        JsonArray convertedArray = new JsonArray();
        SalesTableHandler dummyHandler = new SalesTableHandler(null, null); // For getItemIdAsString

        for (JsonElement saleEl : rawSalesArray) {
            JsonObject originalSale = saleEl.getAsJsonObject();
            JsonObject convertedSale = new JsonObject();

            convertedSale.addProperty("Sale ID", originalSale.get("saleId").getAsString());

            String itemId = originalSale.get("itemId").getAsString();
            String itemName = "Item Not Found";
            if (itemsMapForConversion != null && itemsMapForConversion.containsKey(itemId)) {
                JsonObject item = itemsMapForConversion.get(itemId);
                if(item.has("itemName") && !item.get("itemName").isJsonNull()){
                    itemName = item.get("itemName").getAsString();
                } else {
                    itemName = "Item Name Missing (ID: "+itemId+")";
                }
            } else {
                System.err.println("Item ID " + itemId + " not found in provided itemsMap. Static lookup might be slow or fail if item is deleted.");
                // Fallback (less efficient, consider if itemsMap should always be complete)
                itemName = getNameByIdStatic(ITEMS_FILE_PATH, "itemId", itemId, "itemName", dummyHandler);
            }
            convertedSale.addProperty("Item Name", itemName);
            
            convertedSale.addProperty("Quantity Sold", originalSale.has("quantitySold") ? originalSale.get("quantitySold").getAsString() : "N/A");
            convertedSale.addProperty("Total Sales", originalSale.has("totalSales") ? originalSale.get("totalSales").getAsString() : "N/A");
            
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

    private static String getNameByIdStatic(String filePath, String idKeyToMatch, String targetIdValue, String nameKeyToFetch, SalesTableHandler helperInstance) {
        try {
            JsonArray itemsArray = JsonStorageHelper.loadAsJsonArray(filePath);
            for (JsonElement el : itemsArray) {
                JsonObject itemObj = el.getAsJsonObject();
                String currentItemIdStr = helperInstance.getItemIdAsString(itemObj, idKeyToMatch);

                if (currentItemIdStr != null && currentItemIdStr.equals(targetIdValue)) {
                    return (itemObj.has(nameKeyToFetch) && !itemObj.get(nameKeyToFetch).isJsonNull()) ?
                           itemObj.get(nameKeyToFetch).getAsString() : "Name N/A";
                }
            }
        } catch (Exception e) {
            System.err.println("Error in getNameByIdStatic (ID '" + targetIdValue + "'): " + e.getMessage());
        }
        return "Unknown Item (ID: " + targetIdValue + ")";
    }

    private JsonArray getLatestSalesData() {
        try {
            return JsonStorageHelper.loadAsJsonArray(SALES_FILE_PATH);
        } catch (IOException e) {
            System.out.println(SALES_FILE_PATH + " not found or unreadable. Returning empty. " + e.getMessage());
            return new JsonArray();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(currentPage, "Error parsing " + SALES_FILE_PATH + ". Details: " + e.getMessage(), "JSON Error", JOptionPane.ERROR_MESSAGE);
            return new JsonArray();
        }
    }

    private void refreshSalesTable() {
        JsonArray updatedRawSalesData = getLatestSalesData();
        // Ensure itemsMap is up-to-date, especially if an operation changed stock
        // loadItemsData(); // Called within updateItemStockInFile, and at start of onEdit/onDelete logic
        JsonArray displayData = convertSalesDataForDisplay(updatedRawSalesData, this.itemsMap);
        if (currentPage instanceof TablePage) {
             ((TablePage) currentPage).refreshTableData(displayData);
        } else {
            System.err.println("Warning: currentPage is not TablePage. Cannot refresh.");
        }
    }
}