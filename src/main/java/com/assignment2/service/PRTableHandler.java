package com.assignment2.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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

        AddPage dialog = new AddPage(page, filePath, fieldLabels, dataTypes, fieldOptions, keys);

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
            JOptionPane.showMessageDialog(null, "Failed to load PO file.");
            return;
        }

        // Find original row
        int targetPoId = record.get("poId").getAsInt();

        JsonObject original = null;
        for (JsonElement el : poList) {
            JsonObject obj = el.getAsJsonObject();
            if (obj.get("poId").getAsInt() == targetPoId) {
                original = obj;
                break;
            }
        }

        if (original == null) {
            JOptionPane.showMessageDialog(null, "Original record not found.");
            return;
        }

        Map<String, FieldDefinition> fieldDefs = new LinkedHashMap<>();
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
        context.tableName = "purchase_order";
        context.putMeta("supplierId", supplierId);
        context.putMeta("itemId", itemId);


        new EditDialog(page, updatedData -> {
            // Ensure poId is preserved from the original data
            String poId = context.originalData.get("poId").getAsString();
            updatedData.addProperty("poId", poId);

            try {
                JsonStorageHelper.updateOrInsert("PurchaseOrder.txt", updatedData, "poId");

                JsonArray updatedList = JsonStorageHelper.loadAsJsonArray("PurchaseOrder.txt");
                page.refreshTableData(convert(updatedList));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed to update PO record.");
                e.printStackTrace();
            }
        }, fieldDefs, context, (EditValidator) (newData, ctx) -> {

            // if no changes made, close (won't show Message Dialog)
            if (newData == null || newData.entrySet().isEmpty()) {
                System.out.println("No changes made. Skipping validation.");
                return true;
            }

            String validatedSupplierId = ctx.getMeta("supplierId").toString();
            String validatedItemId = ctx.getMeta("itemId").toString();
            
            //checks if supplier has the item
            boolean isValid = JsonStorageHelper.rowExists("supplier_items.txt", row ->
                row.get("supplierId").getAsString().equals(validatedSupplierId) &&
                row.get("itemId").getAsString().equals(validatedItemId)
            );

            if (!isValid) {
                JOptionPane.showMessageDialog(null, "Selected supplier does not provide this item.");
                return false;
            }

            return true;
        }).setVisible(true);
    }

    @Override
    public void onDelete(JsonObject rowData, String pointerKeyPath) {
        throw new UnsupportedOperationException("onDelete unused");
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
}