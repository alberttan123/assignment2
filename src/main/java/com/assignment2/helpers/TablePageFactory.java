
package com.assignment2.helpers;

import com.assignment2.gui_albert.FieldDefinition;
import com.assignment2.gui_albert.TablePage;
import com.assignment2.gui_xiang.UpdateStockFromApprovedPOs;
import com.assignment2.service.UserTableHandler;
import com.assignment2.service.itemsTableHandler;
import com.assignment2.service.poTableHandler;
import com.assignment2.session.SessionManager;
import com.assignment2.service.StockReportTableHandler;
import com.assignment2.service.PRTableHandler;
import com.assignment2.service.SalesTableHandler;
import com.assignment2.service.SupplierTableHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.assignment2.gui_albert.TablePage;
import com.assignment2.gui_xiang.UpdateStockFromApprovedPOs;
import com.assignment2.service.PRTableHandler;
import com.assignment2.service.StockReportTableHandler;
import com.assignment2.service.SupplierTableHandler;
import com.assignment2.service.UserTableHandler;
import com.assignment2.service.inventoryitemsTableHandler;
import com.assignment2.service.itemsTableHandler;
import com.assignment2.service.poTableHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TablePageFactory {

    public static TablePage createUserTable() {
        TablePage tablePage = null;
        try{
            String filePath = "data/users.txt";
            JsonObject root = JsonStorageHelper.loadAsJsonObject(filePath);
            JsonArray arr = root.getAsJsonArray("users");
            String[] excluded = { "name.fname", "name.lname", "dob.day", "dob.month", "dob.year", "profilePicturePath", "password"};
            List<String> columnOrder = List.of("Full Name", "email", "Birthdate", "role", "createdAt");

            // Combined columns
            Map<String, String> combined = new HashMap<>();
            combined.put("Full Name", "name.fname name.lname");
            combined.put("Birthdate", "dob.day dob.month dob.year");

            String pointerKeyPath = "email";

            tablePage = new TablePage("User Management", true, true, true, excluded, combined, columnOrder, pointerKeyPath, arr, false);

            tablePage.setTableActionHandler(new UserTableHandler(tablePage, tablePage));
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("users.txt not found.");
        }
        return tablePage;
    }

    public static TablePage viewPOTable() {
        TablePage tablePage = null;
        try{
            String filePath = "PurchaseOrder.txt";

            JsonArray arr = JsonStorageHelper.loadAsJsonArray(filePath);
            poTableHandler.setIsApprove(false);
            JsonArray convertedArray = poTableHandler.convert(arr);
            String[] excluded = {};
            List<String> columnOrder = List.of();

            // Combined columns
            Map<String, String> combined = new HashMap<>();
            // combined.put("Full Name", "name.fname name.lname");
            // combined.put("Birthdate", "dob.day dob.month dob.year");

            String pointerKeyPath = null;

            tablePage = new TablePage("Purchase Orders", false, false, false, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);

            tablePage.setTableActionHandler(new poTableHandler(tablePage, false));
            tablePage.setTableActionAdapter(new poTableHandler(tablePage, false));
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("PurchaseOrder.txt not found.");
        }
        return tablePage;
    }

    public static TablePage viewPOTableFM() {
        TablePage tablePage = null;
        try{
            String filePath = "PurchaseOrder.txt";

            JsonArray arr = JsonStorageHelper.loadAsJsonArray(filePath);
            poTableHandler.setIsApprove(false);
            JsonArray convertedArray = poTableHandler.convert(arr);
            String[] excluded = {};
            List<String> columnOrder = List.of();

            // Combined columns
            Map<String, String> combined = new HashMap<>();
            // combined.put("Full Name", "name.fname name.lname");
            // combined.put("Birthdate", "dob.day dob.month dob.year");

            String pointerKeyPath = null;

            tablePage = new TablePage("Purchase Orders", true, false, false, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);

            tablePage.setTableActionHandler(new poTableHandler(tablePage, false));
            tablePage.setTableActionAdapter(new poTableHandler(tablePage, false));
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("PurchaseOrder.txt not found.");
        }
        return tablePage;
    }

    public static TablePage createApprovePOTable() {
        TablePage tablePage = null;
        try{
            String filePath = "PurchaseOrder.txt";
            JsonArray arr = JsonStorageHelper.loadAsJsonArray(filePath);
            poTableHandler.setIsApprove(true);
            JsonArray convertedArray = poTableHandler.convert(arr);
            String[] excluded = {};
            List<String> columnOrder = List.of();

            // Combined columns
            Map<String, String> combined = new HashMap<>();
            // combined.put("Full Name", "name.fname name.lname");
            // combined.put("Birthdate", "dob.day dob.month dob.year");

            String pointerKeyPath = null;

            tablePage = new TablePage("Purchase Orders", true, false, false, excluded, combined, columnOrder, pointerKeyPath, convertedArray, true);

            tablePage.setTableActionHandler(new poTableHandler(tablePage, true));
            tablePage.setTableActionAdapter(new poTableHandler(tablePage, true));
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("PurchaseOrder.txt not found.");
        }
        return tablePage;
    }

    public static TablePage createItemsTable() {
        TablePage tablePage = null;
        try{
            String filePath = "items.txt";
            JsonArray arr = JsonStorageHelper.loadAsJsonArray(filePath);
            // itemsTableHandler.setIsApprove(false);
            JsonArray convertedArray = itemsTableHandler.convert(arr);
            String[] excluded = {};
            List<String> columnOrder = List.of();

            // Combined columns
            Map<String, String> combined = new HashMap<>();
            // combined.put("Full Name", "name.fname name.lname");
            // combined.put("Birthdate", "dob.day dob.month dob.year");

            String pointerKeyPath = "itemId";

            tablePage = new TablePage("Items", true, true, true, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);

            tablePage.setTableActionHandler(new itemsTableHandler(tablePage, tablePage));
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("items.txt not found.");
        }
        return tablePage;
    }

    public static TablePage createInventoryItemsTable() {
        TablePage tablePage = null;
        try{
            String filePath = "items.txt";
            JsonArray arr = JsonStorageHelper.loadAsJsonArray(filePath);
            // itemsTableHandler.setIsApprove(false);
            JsonArray convertedArray = inventoryitemsTableHandler.convert(arr);
            String[] excluded = {};
            List<String> columnOrder = List.of();

            // Combined columns
            Map<String, String> combined = new HashMap<>();
            // combined.put("Full Name", "name.fname name.lname");
            // combined.put("Birthdate", "dob.day dob.month dob.year");

            String pointerKeyPath = "itemId";

            tablePage = new TablePage("Items", true, false, false, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);

            tablePage.setTableActionHandler(new inventoryitemsTableHandler(tablePage, tablePage));
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("items.txt not found.");
        }
        return tablePage;
    }

    public static TablePage createInventoryItemsTablePM() {
        TablePage tablePage = null;
        try{
            String filePath = "items.txt";
            JsonArray arr = JsonStorageHelper.loadAsJsonArray(filePath);
            // itemsTableHandler.setIsApprove(false);
            JsonArray convertedArray = inventoryitemsTableHandler.convert(arr);
            String[] excluded = {};
            List<String> columnOrder = List.of();

            // Combined columns
            Map<String, String> combined = new HashMap<>();
            // combined.put("Full Name", "name.fname name.lname");
            // combined.put("Birthdate", "dob.day dob.month dob.year");

            String pointerKeyPath = "itemId";

            tablePage = new TablePage("Items", false, false, false, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);

            tablePage.setTableActionHandler(new inventoryitemsTableHandler(tablePage, tablePage));
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("items.txt not found.");
        }
        return tablePage;
    }

    public static TablePage createSupplierTable() {
        TablePage tablePage = null;
        try{
            String filePath = "Supplier.txt";
            JsonArray arr = JsonStorageHelper.loadAsJsonArray(filePath);
            // itemsTableHandler.setIsApprove(false);
            JsonArray convertedArray = SupplierTableHandler.convert(arr);
            String[] excluded = {};
            List<String> columnOrder = List.of();

            // Combined columns
            Map<String, String> combined = new HashMap<>();
            // combined.put("Full Name", "name.fname name.lname");
            // combined.put("Birthdate", "dob.day dob.month dob.year");

            String pointerKeyPath = "supplierId";

            tablePage = new TablePage("Suppliers", true, true, true, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);

            tablePage.setTableActionHandler(new SupplierTableHandler(tablePage, tablePage));
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Supplier.txt not found.");
        }
        return tablePage;
    }

    public static TablePage createSupplierTablePM() {
        TablePage tablePage = null;
        try{
            String filePath = "Supplier.txt";
            JsonArray arr = JsonStorageHelper.loadAsJsonArray(filePath);
            // itemsTableHandler.setIsApprove(false);
            JsonArray convertedArray = SupplierTableHandler.convert(arr);
            String[] excluded = {};
            List<String> columnOrder = List.of();

            // Combined columns
            Map<String, String> combined = new HashMap<>();
            // combined.put("Full Name", "name.fname name.lname");
            // combined.put("Birthdate", "dob.day dob.month dob.year");

            String pointerKeyPath = "supplierId";

            tablePage = new TablePage("Suppliers", false, false, false, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);

            tablePage.setTableActionHandler(new SupplierTableHandler(tablePage, tablePage));
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Supplier.txt not found.");
        }
        return tablePage;
    }
    
    public static TablePage createStockReportTable() {
        TablePage tablePage = null;
        try{
            String filePath = "items.txt";
            JsonArray arr = JsonStorageHelper.loadAsJsonArray(filePath);
            // itemsTableHandler.setIsApprove(false);
            JsonArray convertedArray = StockReportTableHandler.convert(arr);
            String[] excluded = {};
            List<String> columnOrder = List.of();

            // Combined columns
            Map<String, String> combined = new HashMap<>();
            // combined.put("Full Name", "name.fname name.lname");
            // combined.put("Birthdate", "dob.day dob.month dob.year");

            String pointerKeyPath = "itemId";

            tablePage = new TablePage("Stock Report", false, false, false, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);

            tablePage.setTableActionHandler(new StockReportTableHandler(tablePage, tablePage));
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("items.txt not found.");
        }
        return tablePage;
    }
         

    public static TablePage createViewItemsTable() {
        TablePage tablePage = null;
        try{
            String filePath = "items.txt";
            JsonArray arr = JsonStorageHelper.loadAsJsonArray(filePath);
            // itemsTableHandler.setIsApprove(false);
            JsonArray convertedArray = itemsTableHandler.convert(arr);
            String[] excluded = {};
            List<String> columnOrder = List.of();

            // Combined columns
            Map<String, String> combined = new HashMap<>();
            // combined.put("Full Name", "name.fname name.lname");
            // combined.put("Birthdate", "dob.day dob.month dob.year");

            String pointerKeyPath = null;

            tablePage = new TablePage("Items", true, false, false, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);

            tablePage.setTableActionHandler(new itemsTableHandler(tablePage, tablePage));
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("items.txt not found.");
        }
        return tablePage;
    }

    public static TablePage createLowStockTable() {
        JsonArray arr;

        // Try loading items.txt
        try {
            arr = JsonStorageHelper.loadAsJsonArray("items.txt");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "items.txt not found.");
            arr = new JsonArray(); // fallback to empty array
        }

        final JsonArray items = arr; // final copy for lambda use

        JsonArray convertedArray = itemsTableHandler.convert(items);
        String[] excluded = {};
        List<String> columnOrder = List.of();
        Map<String, String> combined = new HashMap<>();
        String pointerKeyPath = null;

        final TablePage tablePage = new TablePage("Items", false, false, false, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);
        tablePage.setTableActionHandler(new itemsTableHandler(tablePage, tablePage));

        JTextField thresholdField = new JTextField("6", 5);
        JButton applyButton = new JButton("Apply");

        applyButton.addActionListener(e -> {
            int threshold = 6;
            try {
                threshold = Integer.parseInt(thresholdField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number.");
                return;
            }

            JsonArray filtered = new JsonArray();
            for (JsonElement el : items) {
                JsonObject item = el.getAsJsonObject();
                if (item.has("stockLevel") && item.get("stockLevel").getAsInt() < threshold) {
                    filtered.add(item);
                }
            }

            tablePage.refreshTableData(itemsTableHandler.convert(filtered));
        });
        SwingUtilities.invokeLater(() -> applyButton.doClick());

        tablePage.addToTop(new JLabel(" Show items with stock < "), thresholdField, applyButton);
        return tablePage;
    }

    public static TablePage createUpdateStockTable() {
        JsonArray arr;

        // Try loading items.txt
        try {
            arr = JsonStorageHelper.loadAsJsonArray("items.txt");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "items.txt not found.");
            arr = new JsonArray(); // fallback to empty array
        }

        final JsonArray items = arr; // final copy for lambda use

        JsonArray convertedArray = itemsTableHandler.convert(items);
        String[] excluded = {};
        List<String> columnOrder = List.of();
        Map<String, String> combined = new HashMap<>();
        String pointerKeyPath = null;

        final TablePage tablePage = new TablePage("Items", false, false, false, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);
        tablePage.setTableActionHandler(new itemsTableHandler(tablePage, tablePage));

        // Create Update Stock Button
        JButton updateStockButton = new JButton("Update Stock from Approved POs");
        updateStockButton.addActionListener(e -> {
            UpdateStockFromApprovedPOs.updateStockFromApprovedPOs(tablePage);

            // Reload and filter items after stock update
            try {
                JsonArray updatedItems = JsonStorageHelper.loadAsJsonArray("items.txt");
                tablePage.refreshTableData(itemsTableHandler.convert(updatedItems));

            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(tablePage, "Failed to reload items after stock update.");
            }
        });

        tablePage.addToTop(updateStockButton);
        return tablePage;
    }

    public static TablePage createPRTable() {
        JsonArray arr;

        try {
            arr = JsonStorageHelper.loadAsJsonArray("PurchaseRequest.txt");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "PurchaseRequest.txt not found.");
            arr = new JsonArray(); // fallback to empty array
        }

        final JsonArray items = arr; // final copy for lambda use

        JsonArray convertedArray = PRTableHandler.convert(items);
        String[] excluded = {};
        List<String> columnOrder = List.of("PR ID", "Item", "Quantity", "Supplier", "Required By", "Raised By");
        Map<String, String> combined = new HashMap<>();
        String pointerKeyPath = "prId";

        final TablePage tablePage = new TablePage("Purchase Request",true, true, true, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);
        tablePage.setTableActionHandler(new PRTableHandler(tablePage));

        return tablePage;
    }

    public static TablePage createViewPRTable() {
        JsonArray purchaseRequestsArray; // Holds original PR data

        try {
            purchaseRequestsArray = JsonStorageHelper.loadAsJsonArray("PurchaseRequest.txt");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "PurchaseRequest.txt not found. Cannot load PRs.");
            purchaseRequestsArray = new JsonArray(); // fallback to empty array
        }

        final JsonArray originalPrDataList = purchaseRequestsArray;

        // PRTableHandler.convert() MUST add "prId" (original key) and "Status" to the display objects
        JsonArray convertedDisplayArray = PRTableHandler.convert(originalPrDataList);

        String[] excluded = {};
        List<String> columnOrder = List.of("PR ID", "Item", "Quantity", "Supplier", "Required By", "Raised By", "Status");
        Map<String, String> combined = new HashMap<>();
        String pointerKeyPath = "prId"; // This is the key in ORIGINAL data AND in displayed data for findRowByPointerValue

        final TablePage tablePage = new TablePage("Purchase Request List", false, false, false,
                excluded, combined, columnOrder,
                pointerKeyPath, convertedDisplayArray, true); // allowApproveReject = true for PRs

        tablePage.setTableActionHandler(new PRTableHandler(tablePage));


        if ("purchase_manager".equals(SessionManager.getUserRole())) {
            JButton generatePOButton = new JButton("Generate PO from Selected PR");

            generatePOButton.addActionListener(e -> {
                String selectedOriginalPrId = tablePage.getSelectedPointerValue(); // Gets original prId (e.g., "202")

                if (selectedOriginalPrId == null) {
                    JOptionPane.showMessageDialog(tablePage, "Please select a PR row first.");
                    return;
                }

                tablePage.setPointerKeyPath("PR ID");
                JsonObject displayedPrData = tablePage.findRowByPointerValue(selectedOriginalPrId);
                tablePage.setPointerKeyPath("prId");

                if (displayedPrData == null) {
                    JOptionPane.showMessageDialog(tablePage, "Could not retrieve details for selected PR (ID: " + selectedOriginalPrId + "). It might have been removed or data is inconsistent.");
                    System.err.println("Failed to find displayed data for original PR ID: " + selectedOriginalPrId + " using findRowByPointerValue.");
                    return;
                }

                if (JsonStorageHelper.getListOfInt("PurchaseOrder.txt", "prId").contains(Integer.parseInt(selectedOriginalPrId))){
                    JOptionPane.showMessageDialog(tablePage, "This PR (ID: " + selectedOriginalPrId + ") already has a PO linked to it.");
                    System.err.println("Failed to create PR from original PR ID: " + selectedOriginalPrId + " . This PR already has a PO linked to it.");
                    return;
                }

                try {
                    // Pass the original PR ID and the full list of original PRs
                    JsonObject newPO = createPoFromPr(selectedOriginalPrId, originalPrDataList);

                    JsonArray currentPoArray;
                    try {
                        currentPoArray = JsonStorageHelper.loadAsJsonArray("PurchaseOrder.txt");
                    } catch (IOException ex) {
                        System.out.println("PurchaseOrder.txt not found or empty, creating new array for PO.");
                        currentPoArray = new JsonArray();
                    }
                    currentPoArray.add(newPO);
                    JsonStorageHelper.saveToJson("PurchaseOrder.txt", currentPoArray);
                    JOptionPane.showMessageDialog(tablePage, "PO #" + newPO.get("poId").getAsString() + " generated successfully from PR #" + selectedOriginalPrId);

                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(tablePage, "Failed to generate PO: " + ex.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(tablePage, "An unexpected error occurred while generating PO: " + ex.getMessage());
                }
            });

            tablePage.addToTop(generatePOButton);
        }
        tablePage.setVisible(true);
        return tablePage;
    }

    private static JsonObject createPoFromPr(String originalPrId, JsonArray originalPrList) throws IOException {
        // Find the original PR from PurchaseRequest.txt using its ID
        JsonObject originalPR = findOriginalPrById(originalPrId, originalPrList);
        if (originalPR == null) {
            throw new IOException("Original PR data not found for PR ID: " + originalPrId);
        }

        JsonObject newPO = new JsonObject();
        newPO.addProperty("poId", JsonStorageHelper.getNextId("PurchaseOrder.txt", "poId"));
        newPO.addProperty("prId", Integer.parseInt(originalPR.get("prId").getAsString()));
        newPO.addProperty("itemId", Integer.parseInt(originalPR.get("itemId").getAsString()));
        newPO.addProperty("supplierId", Integer.parseInt(originalPR.get("supplierId").getAsString()));
        newPO.addProperty("quantity", Integer.parseInt(originalPR.get("quantity").getAsString()));
        newPO.addProperty("status", "Pending");

        int generatedByUserId = 1;
        try {
            generatedByUserId = Integer.parseInt(SessionManager.getUserId());
        } catch (Exception e) {
            System.err.println("Could not get valid userId from SessionManager, using default 1. Error: " + e.getMessage());
        }
        newPO.addProperty("generatedByUserId", generatedByUserId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        newPO.addProperty("createdAt", LocalDateTime.now().format(formatter));
        newPO.add("approvedByUserId", JsonNull.INSTANCE);
        newPO.addProperty("approvedAt", "");

        return newPO;
    }

     public static JsonObject findOriginalPrById(String prIdToFind, JsonArray originalPrList) {
        if (prIdToFind == null || originalPrList == null) {
            System.err.println("findOriginalPrById: prIdToFind or originalPrList is null. Cannot search.");
            return null;
        }

        if (prIdToFind.trim().isEmpty()) {
            System.err.println("findOriginalPrById: prIdToFind is empty. Cannot search.");
            return null;
        }

        for (JsonElement element : originalPrList) {
            if (element == null || !element.isJsonObject()) {
                System.err.println("findOriginalPrById: Encountered a non-JsonObject element in originalPrList. Skipping.");
                continue;
            }

            JsonObject currentPr = element.getAsJsonObject();

            // check if the current PR JsonObject has the "prId" field
            if (currentPr.has("prId")) {
                JsonElement prIdElement = currentPr.get("prId");

                // ensure the "prId" field is not null and is a string
                if (prIdElement != null && !prIdElement.isJsonNull() && prIdElement.isJsonPrimitive() && prIdElement.getAsJsonPrimitive().isString()) {
                    String currentPrIdValue = prIdElement.getAsString();

                    // Compare the current PR's ID with the ID we're looking for
                    if (prIdToFind.equals(currentPrIdValue)) {
                        return currentPr; // Found the matching pr, return its JsonObject
                    }
                } else {
                    System.err.println("findOriginalPrById: Encountered a PR with missing, null, or non-string 'prId' field: " + currentPr.toString());
                }
            } else {
                // This indicates an issue with the data format in PurchaseRequest.txt
                System.err.println("findOriginalPrById: Encountered a PR JsonObject without a 'prId' field: " + currentPr.toString());
            }
        }

        // If the loop completes without finding a match
        System.out.println("findOriginalPrById: PR with ID '" + prIdToFind + "' not found in the provided list.");
        return null;
    }
}