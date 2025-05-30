package com.assignment2.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JOptionPane;

import com.assignment2.gui_albert.TablePage;
import com.assignment2.helpers.JsonStorageHelper;
import com.assignment2.session.SessionManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class poTableHandler extends TableActionAdapter{

    private TablePage page;
    private String filePath = "resources/PurchaseOrder.txt";

    public poTableHandler(TablePage page){
        this.page = page;
    }

    @Override
    public void onAdd() {
        throw new UnsupportedOperationException("onAdd unused");
    }

    @Override
    public void onEdit(JsonObject rowData) {
        throw new UnsupportedOperationException("Unimplemented method 'onEdit'");
        //only can modify quantity/supplier
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
                page.refreshTableData(poList);
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
                page.refreshTableData(poList);
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
            JsonObject converted = new JsonObject();

            converted.addProperty("poId", original.get("poId").getAsInt());
            converted.addProperty("prId", original.get("prId").getAsInt());

            // Convert ID fields to names
            converted.addProperty("Item", getNameById("resources/items.txt", "itemId", original.get("itemId").getAsInt(), "itemName"));
            converted.addProperty("Supplier", getNameById("resources/Supplier.txt", "supplierId", original.get("supplierId").getAsInt(), "name"));
            converted.addProperty("Requested By", getNameById("data/users.txt", "userId", original.get("generatedByUserId").getAsInt(), "email"));

            int quantity = original.get("quantity").getAsInt();
            converted.addProperty("Quantity", quantity);

            String status = original.get("status").getAsString();
            converted.addProperty("Status", status);

            // Optional: if approved
            if (original.has("approvedByUserId") && !original.get("approvedByUserId").isJsonNull()) {
                converted.addProperty("Approved By", getNameById("data/users.txt", "userId", original.get("generatedByUserId").getAsInt(), "email"));
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
