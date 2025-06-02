package com.assignment2.gui_xiang;

import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import com.assignment2.gui_albert.TablePage;

import com.assignment2.helpers.JsonStorageHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UpdateStockFromApprovedPOs {

    public static void updateStockFromApprovedPOs(TablePage currentPage) {
        try {
            String poFilePath = "PurchaseOrder.txt";
            String itemFilePath = "items.txt";
            JsonArray poArray = JsonStorageHelper.loadAsJsonArray(poFilePath);
            JsonArray itemArray = JsonStorageHelper.loadAsJsonArray(itemFilePath);

            // Build item map: itemId -> itemObject
            Map<Integer, JsonObject> itemMap = new HashMap<>();
            for (JsonElement el : itemArray) {
                JsonObject item = el.getAsJsonObject();
                itemMap.put(item.get("itemId").getAsInt(), item);
            }

            StringBuilder summary = new StringBuilder();
            int updateCount = 0;

            for (JsonElement el : poArray) {
                JsonObject po = el.getAsJsonObject();

                // Only update if Approved and not already updated
                if ("Approved".equals(po.get("status").getAsString()) &&
                    (!po.has("stockUpdated") || !po.get("stockUpdated").getAsBoolean())) {

                    int itemId = po.get("itemId").getAsInt();
                    int quantity = po.get("quantity").getAsInt();

                    JsonObject item = itemMap.get(itemId);
                    if (item != null) {
                        int currentStock = Integer.parseInt(item.get("stockLevel").getAsString());
                        item.addProperty("stockLevel", String.valueOf(currentStock + quantity));

                        // Mark this PO as updated
                        po.addProperty("stockUpdated", true);

                        summary.append(String.format("Item %d (%s): +%d units\n",
                            itemId,
                            item.get("itemName").getAsString(),
                            quantity));

                        updateCount++;
                    }
                }
            }

            // If there's nothing to update, show info dialog
            if (updateCount == 0) {
                JOptionPane.showMessageDialog(currentPage, "No approved and unprocessed POs found.");
                return;
            }

            // Confirm with user
            int confirm = JOptionPane.showConfirmDialog(
                currentPage,
                "Update stock for " + updateCount + " approved POs?\n\n" + summary.toString(),
                "Confirm Stock Update",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                JsonStorageHelper.saveToJson(itemFilePath, itemArray);
                JsonStorageHelper.saveToJson(poFilePath, poArray);
                JOptionPane.showMessageDialog(currentPage, "Stock successfully updated.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(currentPage, "Failed to update stock from POs.");
            e.printStackTrace();
        }
    }
}
