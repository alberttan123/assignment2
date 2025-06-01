package com.assignment2.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.assignment2.gui_albert.EditDialog;
import com.assignment2.gui_albert.FieldDefinition;
import com.assignment2.gui_albert.TablePage;
import com.assignment2.helpers.EditDialogContext;
import com.assignment2.helpers.JsonStorageHelper;
import com.assignment2.session.SessionManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class poTableHandler extends TableActionAdapter{

    private TablePage page;
    private static boolean isApprove;
    private static String filePath = "PurchaseOrder.txt";
    private Map<String, JsonObject> originalDataMap = new LinkedHashMap<>();

    public poTableHandler(TablePage page, boolean isApprove){
        this.page = page;
        poTableHandler.isApprove = isApprove;
    }

    public static void setIsApprove(boolean isApprove){
        poTableHandler.isApprove = isApprove;
    }

    @Override
    public void onAdd() {
        throw new UnsupportedOperationException("onAdd unused");
    }

    @Override
    public void onEdit(JsonObject record) {
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

    @Override
    public void onDelete(JsonObject rowData, String pointerKeyPath) {
        throw new UnsupportedOperationException("onDelete unused");
    }

    @Override
    public void onApprove(JsonObject row) {
        try {
            JsonArray poList = JsonStorageHelper.loadAsJsonArray(filePath);
            int targetPoId = row.get("poId").getAsInt();

            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            boolean found = false;

            for (int i = 0; i < poList.size(); i++) {
                JsonObject po = poList.get(i).getAsJsonObject();
                if (po.get("poId").getAsInt() == targetPoId) {
                    if (!po.get("status").getAsString().equalsIgnoreCase("Pending")) {
                        JOptionPane.showMessageDialog(null, "This PO is already processed.");
                        return;
                    }

                    po.addProperty("status", "Approved");
                    po.addProperty("approvedBy", SessionManager.getCurrentUser().get("userId").getAsInt());
                    po.addProperty("approvedAt", now);
                    found = true;
                    break;
                }
            }

            if (found) {
                JsonStorageHelper.saveToJson(filePath, poList);
                JOptionPane.showMessageDialog(null, "PO approved successfully!");
                page.refreshTableData(convert(poList));
            }

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to approve PO.");
        }
    }

    @Override
    public void onReject(JsonObject row) {
        try {
            JsonArray poList = JsonStorageHelper.loadAsJsonArray(filePath);
            int targetPoId = row.get("poId").getAsInt();

            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            boolean found = false;

            for (int i = 0; i < poList.size(); i++) {
                JsonObject po = poList.get(i).getAsJsonObject();
                if (po.get("poId").getAsInt() == targetPoId) {
                    if (!po.get("status").getAsString().equalsIgnoreCase("Pending")) {
                        JOptionPane.showMessageDialog(null, "This PO is already processed.");
                        return;
                    }

                    po.addProperty("status", "Rejected");
                    po.addProperty("approvedBy", SessionManager.getCurrentUser().get("userId").getAsInt());
                    po.addProperty("approvedAt", now);
                    found = true;
                    break;
                }
            }

            if (found) {
                JsonStorageHelper.saveToJson(filePath, poList);
                JOptionPane.showMessageDialog(null, "PO rejected successfully!");
                page.refreshTableData(convert(poList));
            }

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to approve PO.");
        }
    }

    public static JsonArray convert(JsonArray rawArray) {
        JsonArray convertedArray = new JsonArray();

        for (JsonElement el : rawArray) {
            JsonObject original = el.getAsJsonObject();

            if (isApprove && !original.get("status").getAsString().equalsIgnoreCase("Pending")) {
                continue; // if isApprove = true, only show PO with status "Pending"
            }

            JsonObject converted = new JsonObject();

            converted.addProperty("poId", original.get("poId").getAsInt());
            converted.addProperty("prId", original.get("prId").getAsInt());

            // Convert ID fields to names
            converted.addProperty("Item", getNameById("items.txt", "itemId", original.get("itemId").getAsInt(), "itemName"));
            converted.addProperty("Supplier", getNameById("Supplier.txt", "supplierId", original.get("supplierId").getAsInt(), "name"));
            converted.addProperty("Requested By", getNameById("/data/users.txt", "userId", original.get("generatedByUserId").getAsInt(), "email"));

            int quantity = original.get("quantity").getAsInt();
            converted.addProperty("Quantity", quantity);

            String status = original.get("status").getAsString();
            converted.addProperty("Status", status);

            // Optional: if approved
            if (original.has("approvedByUserId") && !original.get("approvedByUserId").isJsonNull()) {
                converted.addProperty("Approved By", getNameById("/data/users.txt", "userId", original.get("generatedByUserId").getAsInt(), "email"));
            } else {
                converted.addProperty("Approved By", "—");
            }

            // Date fields (just date)
            String createdAt = original.get("createdAt").getAsString().split(" ")[0];
            converted.addProperty("Created At", createdAt);

            if (original.has("approvedAt") && !original.get("approvedAt").isJsonNull()) {
                String approvedAt = original.get("approvedAt").getAsString().split(" ")[0];
                converted.addProperty("Approved At", approvedAt);
            } else {
                converted.addProperty("Approved At", "—");
            }

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
}