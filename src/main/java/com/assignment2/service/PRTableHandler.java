package com.assignment2.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.assignment2.gui_albert.AddPage;
import com.assignment2.gui_albert.EditDialog;
import com.assignment2.gui_albert.FieldDefinition;
import com.assignment2.gui_albert.TablePage;
import com.assignment2.helpers.EditDialogContext;
import com.assignment2.helpers.EditValidator;
import com.assignment2.helpers.JsonStorageHelper;
import com.assignment2.session.SessionManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class PRTableHandler implements TableActionHandler{

    private TablePage page;
    private static String filePath = "PurchaseRequest.txt";
    private Map<String, JsonObject> originalDataMap = new LinkedHashMap<>();

    public PRTableHandler(TablePage page){
        this.page = page;
    }

    @Override
    public void onAdd() {
        LinkedHashMap<String, String> fieldLabels = new LinkedHashMap<>();
        fieldLabels.put("Item Name", "itemId");
        fieldLabels.put("Quantity", "quantity");
        fieldLabels.put("Supplier", "supplierId");
        fieldLabels.put("Required By", "requiredBy");

        Map<String, String> dataTypes = Map.of(
            "itemId", "dropdown",
            "quantity", "int",
            "supplierId", "dropdown",
            "requiredBy", "date"
        );

        Map<String, Object> fieldOptions = new HashMap<>();
        try {
            fieldOptions.put("itemId", JsonStorageHelper.getDropdownOptions("items.txt", "itemId", "itemName"));
            fieldOptions.put("supplierId", JsonStorageHelper.getDropdownOptions("Supplier.txt", "supplierId", "name"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to load dropdown options.");
            e.printStackTrace();
        }

        Map<String, Function<String, Boolean>> validationRules = new HashMap<>();
        validationRules.put("quantity", val -> {
            try { return Integer.parseInt(val) > 0; } catch (Exception e) { return false; }
        });
        validationRules.put("price", val -> {
            try { return Double.parseDouble(val) >= 0; } catch (Exception e) { return false; }
        });
        validationRules.put("requiredBy", val -> {
            try {
                return LocalDate.parse(val).isAfter(LocalDate.now().minusDays(1));
            } catch (Exception e) {
                return false;
            }
        });

        // String primaryKey = "prId";
        JsonArray keys = new JsonArray();

        JsonObject idDef = new JsonObject();
        idDef.addProperty("primaryKey", "prId");
        idDef.addProperty("filePath", "PurchaseRequest.txt");

        JsonObject userDef = new JsonObject();
        userDef.addProperty("primaryKey", "userId");
        userDef.addProperty("entry", SessionManager.getUserId());
        userDef.addProperty("saveAsKey", "raisedByUserId");

        keys.add(idDef);
        keys.add(userDef);

        AddPage dialog = new AddPage(page, filePath, fieldLabels, dataTypes, fieldOptions, validationRules, keys);

        // Refresh after dialog closes
        JsonArray updatedList = getLatestData();
        ((TablePage) page).refreshTableData(convert(updatedList));
    }

    @Override
    public void onEdit(JsonObject record) {
        String itemName = record.get("Item").getAsString();
        String supplierName = record.get("Supplier").getAsString();
        String itemId = JsonStorageHelper.lookupValueByLabel("items.txt", "itemName", "itemId", itemName);
        String supplierId = JsonStorageHelper.lookupValueByLabel("Supplier.txt", "name", "supplierId", supplierName);
        //ahhhhhhhhhhhhh

        JsonArray poList;
        try {
            poList = JsonStorageHelper.loadAsJsonArray(filePath);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to load PR file.");
            return;
        }

        // Find original row
        int targetPoId = record.get("PR ID").getAsInt();

        JsonObject original = null;
        for (JsonElement el : poList) {
            JsonObject obj = el.getAsJsonObject();
            if (obj.get("prId").getAsInt() == targetPoId) {
                original = obj;
                break;
            }
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
        fieldDefs.put("supplierId", FieldDefinition
            .dropdown("Supplier.txt", "name", "supplierId")
            .withLabel("Supplier")
            .withKey("supplierId")
            .required());
        fieldDefs.put("Quantity", FieldDefinition
            .of("int")
            .withLabel("Quantity")
            .withKey("quantity")
            .required());

        EditDialogContext context = new EditDialogContext();
        context.originalData = original.deepCopy();
        context.editedData = new JsonObject();
        context.editedData.addProperty("supplierId", supplierId);
        context.editedData.addProperty("Quantity", original.get("quantity").getAsString());
        context.editedData.addProperty("itemId", itemId);
        context.tableName = "purchase_requisition";
        context.putMeta("supplierId", supplierId);
        context.putMeta("itemId", itemId);


        new EditDialog(page, updatedData -> {
            // Ensure poId is preserved from the original data
            String poId = context.originalData.get("prId").getAsString();
            updatedData.addProperty("prId", poId);

            try {
                JsonStorageHelper.updateOrInsert("PurchaseRequest.txt", updatedData, "prId");

                JsonArray updatedList = JsonStorageHelper.loadAsJsonArray("PurchaseRequest.txt");
                page.refreshTableData(convert(updatedList));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed to update PR record.");
                e.printStackTrace();
            }
        }, fieldDefs, context, (EditValidator) (newData, ctx) -> {
            return true;
        }).setVisible(true);
    }

    @Override
    public void onDelete(JsonObject rowData, String pointerKeyPath) {
        System.out.println(rowData);
        String keyVal = rowData.get("PR ID").getAsString();
            deleteRowFromJson(keyVal, pointerKeyPath);
            page.refreshTableData(convert(getLatestData()));
    }

    public static JsonArray convert(JsonArray rawArray) {
        JsonArray convertedArray = new JsonArray();

        for (JsonElement el : rawArray) {
            JsonObject original = el.getAsJsonObject();

            JsonObject converted = new JsonObject();

            converted.addProperty("PR ID", original.get("prId").getAsInt());

            // Convert ID fields to names
            converted.addProperty("Item", getNameById("items.txt", "itemId", original.get("itemId").getAsInt(), "itemName"));
            converted.addProperty("Quantity", original.get("quantity").getAsString());
            converted.addProperty("Supplier", getNameById("Supplier.txt", "supplierId", original.get("supplierId").getAsInt(), "name"));
            converted.addProperty("Required By", original.get("requiredBy").getAsString());
            converted.addProperty("Raised By", getNameById("data/users.txt", "userId", Integer.parseInt(original.get("raisedByUserId").getAsString()), "email"));

            convertedArray.add(converted);
        }

        return convertedArray;
    }

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

    private JsonArray getLatestData() {
        JsonArray data = null;
        try {
            data = JsonStorageHelper.loadAsJsonArray(filePath);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to load PO file.");
            System.out.println("File not found: " + filePath);;
            e.printStackTrace();
        }
        return data;
    }

    
    private void deleteRowFromJson(String keyValue, String pointerKeyPath) {
        try {
            JsonArray arr = getLatestData();

            for (int i = 0; i < arr.size(); i++) {
                JsonObject obj = arr.get(i).getAsJsonObject();
                String value = getNestedValue(obj, pointerKeyPath);
                if (value.equals(keyValue)) {
                    arr.remove(i);
                    JsonStorageHelper.saveToJson(filePath, arr);
                    break;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(page, "Failed to delete row from JSON.");
            e.printStackTrace();
        }
    }

    private String getNestedValue(JsonObject obj, String path) {
        String[] parts = path.split("\\.");
        JsonElement current = obj;

        for (String part : parts) {
            if (current != null && current.isJsonObject()) {
                current = current.getAsJsonObject().get(part);
            } else {
                return "";
            }
        }

        return current != null && !current.isJsonNull() ? current.getAsString() : "";
    }

    private static JsonObject findOriginalPrById(String prIdToFind, JsonArray allPrs) {
        if (prIdToFind == null || allPrs == null) {
            return null;
        }
        for (JsonElement element : allPrs) {
            JsonObject pr = element.getAsJsonObject();
            if (pr.has("prId") && pr.get("prId").getAsString().equals(prIdToFind)) {
                return pr;
            }
        }
        return null; // Not found
    }

    private static JsonObject createPoFromPr(JsonObject prDisplayData, JsonArray originalPrList) throws IOException {
        // get "PR ID" from display data
        // the pointerValue from tablePage.getSelectedPointerValue() is already the prId
        String prIdString = prDisplayData.get("PR ID").getAsString();

        // Find the original PR from PurchaseRequest.txt using its ID
        JsonObject originalPR = findOriginalPrById(prIdString, originalPrList);
        if (originalPR == null) {
            throw new IOException("Original PR data not found for PR ID: " + prIdString);
        }

        JsonObject newPO = new JsonObject();
        newPO.addProperty("poId", JsonStorageHelper.getNextId("PurchaseOrder.txt", "poId"));
        newPO.addProperty("prId", Integer.parseInt(originalPR.get("prId").getAsString())); // From original PR
        newPO.addProperty("itemId", Integer.parseInt(originalPR.get("itemId").getAsString())); // From original PR
        newPO.addProperty("supplierId", Integer.parseInt(originalPR.get("supplierId").getAsString())); // From original PR
        newPO.addProperty("quantity", Integer.parseInt(originalPR.get("quantity").getAsString())); // From original PR

        newPO.addProperty("status", "Pending"); // New POs start as Pending

        // Assuming SessionManager.getUserId() returns the current user's ID as an int or parsable string
        int generatedByUserId = 1; // Default or placeholder
        try {
            // Replace with your actual SessionManager call
            generatedByUserId = Integer.parseInt(SessionManager.getUserId()); 
        } catch (NumberFormatException | NullPointerException e) {
            System.err.println("Could not get valid userId from SessionManager, using default: 1. Error: " + e.getMessage());
            // Handle error or use a default/guest user ID
        }
        newPO.addProperty("generatedByUserId", generatedByUserId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        newPO.addProperty("createdAt", LocalDateTime.now().format(formatter));

        newPO.add("approvedByUserId", JsonNull.INSTANCE); // No approver yet
        newPO.addProperty("approvedAt", ""); // No approval date yet

        return newPO;
    }
}