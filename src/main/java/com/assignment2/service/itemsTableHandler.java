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

public class itemsTableHandler {

    private JFrame currentPage;
    private TablePage page;
    private String jsonFilePath = "/data/users.txt";

    public itemsTableHandler(JFrame currentPage, TablePage page) {
        this.currentPage = currentPage;
        this.page = page;
    }
    
    @Override
    public void onAdd() {
        String itemName = record.get("Item").getAsString();
        String supplierName = record.get("Supplier").getAsString();

        // Find original row
        String itemId = JsonStorageHelper.lookupValueByLabel("items.txt", "itemName", "itemId", itemName);
        String supplierId = JsonStorageHelper.lookupValueByLabel("Supplier.txt", "name", "supplierId", supplierName);
        String compositeKey = supplierId + "-" + itemId;

        JsonArray poList;
        try {
            poList = JsonStorageHelper.loadAsJsonArray(filePath);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to load PO file.");
            return;
        }

        JsonObject original = null;
        for (JsonElement el : poList) {
            JsonObject obj = el.getAsJsonObject();
            String id = obj.get("supplierId").getAsString() + "-" + obj.get("itemId").getAsString();
            originalDataMap.put(id, obj);
            if (id.equals(compositeKey)) {
                original = obj;
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

        new EditDialog(null, updatedData -> {
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
        }, fieldDefs, context).setVisible(true);
    }
}
