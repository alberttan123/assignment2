package com.assignment2.gui_albert;

import com.assignment2.helpers.JsonStorageHelper;
import com.assignment2.service.InventoryChange;
import com.assignment2.service.PaymentService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class InventoryChangesFrame extends JFrame {

    private static final String ITEMS_FILE_PATH = "items.txt";
    private static final String ITEMS_OLD_FILE_PATH = "itemsOld.txt";

    private JTable changesTable;
    private DefaultTableModel tableModel;
    private List<InventoryChange> detectedChanges;

    public InventoryChangesFrame() {
        setTitle("Verify Inventory Changes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose on close, don't exit app
        setLocationRelativeTo(null); // Center on screen

        initComponents();
        loadAndCompareInventories();
        populateTable();
    }

    private void initComponents() {
        tableModel = new DefaultTableModel(
                new Object[]{"Item ID", "Item Name", "Change Type", "Old Value", "New Value"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        changesTable = new JTable(tableModel);
        changesTable.setFillsViewportHeight(true);
        changesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(changesTable);

        JButton approveButton = new JButton("Approve Changes");
        approveButton.addActionListener(e -> approveChangesAction());

        JButton rejectButton = new JButton("Acknowledge & Close (No Action)");
        rejectButton.addActionListener(e -> dispose()); // Just close the window

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private String getItemIdAsString(JsonObject itemObject) {
        JsonElement idElement = itemObject.get("itemId");
        if (idElement == null || idElement.isJsonNull()) return null;
        if (idElement.isJsonPrimitive() && idElement.getAsJsonPrimitive().isString()) {
            return idElement.getAsString();
        } else if (idElement.isJsonPrimitive() && idElement.getAsJsonPrimitive().isNumber()) {
            return idElement.getAsNumber().toString();
        }
        return null;
    }

    private void loadAndCompareInventories() {
        detectedChanges = new ArrayList<>();
        JsonArray currentItemsArray;
        JsonArray oldItemsArray = null;

        try {
            currentItemsArray = JsonStorageHelper.loadAsJsonArray(ITEMS_FILE_PATH);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading current inventory (items.txt): " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            currentItemsArray = new JsonArray(); // Work with empty if current fails
        }
        if (!JsonStorageHelper.checkFileExists(ITEMS_OLD_FILE_PATH)) {
            JOptionPane.showMessageDialog(this,
                    "No previous inventory snapshot (itemsOld.txt) found. Assuming all current items are new or this is the first run.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            // Treat all current items as "NEW" if no old file
            for (JsonElement itemEl : currentItemsArray) {
                JsonObject currentItem = itemEl.getAsJsonObject();
                String itemId = getItemIdAsString(currentItem);
                if (itemId != null) {
                    String itemName = currentItem.has("itemName") ? currentItem.get("itemName").getAsString() : "N/A";
                    detectedChanges.add(new InventoryChange(itemId, itemName, InventoryChange.ChangeType.NEW, "-", "Exists"));
                }
            }
            // Create itemsOld.txt from current items.txt for next time
            try {
                Files.copy(JsonStorageHelper.getDataPath(ITEMS_FILE_PATH), JsonStorageHelper.getDataPath(ITEMS_OLD_FILE_PATH), StandardCopyOption.REPLACE_EXISTING);
                System.out.println(ITEMS_OLD_FILE_PATH + " created from " + ITEMS_FILE_PATH);
            } catch (IOException ioException) {
                System.err.println("Could not create " + ITEMS_OLD_FILE_PATH + ": " + ioException.getMessage());
                JOptionPane.showMessageDialog(this, "Could not create backup " + ITEMS_OLD_FILE_PATH, "File Error", JOptionPane.WARNING_MESSAGE);
            }
            return; // No further comparison needed
        }

        try {
            oldItemsArray = JsonStorageHelper.loadAsJsonArray(ITEMS_OLD_FILE_PATH);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading previous inventory (itemsOld.txt): " + e.getMessage() +
                    "\nComparison might be incomplete.", "File Error", JOptionPane.WARNING_MESSAGE);
            // If old items can't be loaded, we can't do a proper comparison.
            // Could treat all current as new, or show an error and stop.
            // For now, let's show warning and proceed with currentItems as mostly "new".
             for (JsonElement itemEl : currentItemsArray) {
                JsonObject currentItem = itemEl.getAsJsonObject();
                String itemId = getItemIdAsString(currentItem);
                if (itemId != null) {
                    String itemName = currentItem.has("itemName") ? currentItem.get("itemName").getAsString() : "N/A";
                    detectedChanges.add(new InventoryChange(itemId, itemName, InventoryChange.ChangeType.NEW, "Old file unreadable", "Exists"));
                }
            }
            return;
        }

        // --- Perform Comparison ---
        Map<String, JsonObject> oldItemsMap = new HashMap<>();
        for (JsonElement itemEl : oldItemsArray) {
            JsonObject item = itemEl.getAsJsonObject();
            String itemId = getItemIdAsString(item);
            if (itemId != null) {
                oldItemsMap.put(itemId, item);
            }
        }

        Map<String, JsonObject> currentItemsMap = new HashMap<>();
         for (JsonElement itemEl : currentItemsArray) {
            JsonObject item = itemEl.getAsJsonObject();
            String itemId = getItemIdAsString(item);
            if (itemId != null) {
                currentItemsMap.put(itemId, item);
            }
        }


        // 1. Check for MODIFIED and NEW items
        for (JsonElement currentItemEl : currentItemsArray) {
            JsonObject currentItem = currentItemEl.getAsJsonObject();
            String currentItemId = getItemIdAsString(currentItem);
            if (currentItemId == null) continue;

            String currentItemName = currentItem.has("itemName") ? currentItem.get("itemName").getAsString() : "N/A";

            if (oldItemsMap.containsKey(currentItemId)) {
                // Item exists in both, check for modifications
                JsonObject oldItem = oldItemsMap.get(currentItemId);
                boolean nameChanged = false;
                boolean stockChanged = false;
                boolean priceChanged = false;

                String oldItemName = oldItem.has("itemName") ? oldItem.get("itemName").getAsString() : "N/A";
                String oldStock = oldItem.has("stockLevel") ? oldItem.get("stockLevel").getAsString() : "N/A";
                String oldPrice = oldItem.has("sellingPrice") ? oldItem.get("sellingPrice").getAsString() : "N/A";

                String currentStock = currentItem.has("stockLevel") ? currentItem.get("stockLevel").getAsString() : "N/A";
                String currentPrice = currentItem.has("sellingPrice") ? currentItem.get("sellingPrice").getAsString() : "N/A";

                if (!oldItemName.equals(currentItemName)) {
                    detectedChanges.add(new InventoryChange(currentItemId, currentItemName, InventoryChange.ChangeType.MODIFIED_NAME, oldItemName, currentItemName));
                    nameChanged = true;
                }
                if (!oldStock.equals(currentStock)) {
                     detectedChanges.add(new InventoryChange(currentItemId, currentItemName, InventoryChange.ChangeType.MODIFIED_STOCK, oldStock, currentStock));
                     stockChanged = true;
                }
                if (!oldPrice.equals(currentPrice)) {
                    detectedChanges.add(new InventoryChange(currentItemId, currentItemName, InventoryChange.ChangeType.MODIFIED_PRICE, oldPrice, currentPrice));
                    priceChanged = true;
                }
                // Note: If multiple fields changed for the same item, they'll appear as separate rows.

                oldItemsMap.remove(currentItemId); // Remove from map to track processed old items
            } else {
                // Item is NEW (in current, not in old)
                detectedChanges.add(new InventoryChange(currentItemId, currentItemName, InventoryChange.ChangeType.NEW, "-", "Exists"));
            }
        }

        // 2. Check for REMOVED items
        // Any items remaining in oldItemsMap were not in currentItemsArray
        for (Map.Entry<String, JsonObject> entry : oldItemsMap.entrySet()) {
            JsonObject oldItem = entry.getValue();
            String oldItemId = entry.getKey();
            String oldItemName = oldItem.has("itemName") ? oldItem.get("itemName").getAsString() : "N/A";
            detectedChanges.add(new InventoryChange(oldItemId, oldItemName, InventoryChange.ChangeType.REMOVED, "Existed", "-"));
        }
    }

    private void populateTable() {
        tableModel.setRowCount(0); // Clear existing rows

        if (detectedChanges.isEmpty()) {
            Vector<Object> row = new Vector<>();
            row.add("N/A");
            row.add("No Changes Detected");
            row.add("INFO");
            row.add("-");
            row.add("-");
            tableModel.addRow(row);
            return;
        }

        for (InventoryChange change : detectedChanges) {
            Vector<Object> row = new Vector<>();
            row.add(change.getItemId());
            row.add(change.getItemName());
            row.add(change.getTypeDescription());
            row.add(change.getOldValue() != null ? change.getOldValue() : "N/A");
            row.add(change.getNewValue() != null ? change.getNewValue() : "N/A");
            tableModel.addRow(row);
        }
    }

    private void approveChangesAction() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Approving changes will update the baseline (itemsOld.txt) to match the current inventory (items.txt).\n" +
                "This means these changes will not be shown again in the next verification.\n\n" +
                "Are you sure you want to approve and update the baseline?",
                "Confirm Approval", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Copy current items.txt to itemsOld.txt, overwriting it
                Files.copy(JsonStorageHelper.getDataPath(ITEMS_FILE_PATH), JsonStorageHelper.getDataPath(ITEMS_OLD_FILE_PATH), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Inventory changes approved. Baseline (itemsOld.txt) updated.", "Approval Successful", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Close the window
                JOptionPane.showMessageDialog(this, "Payments processed.", "Approval Successful", JOptionPane.INFORMATION_MESSAGE);
                PaymentService.processPayments(); //process payments after accepting inventory changes
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to update itemsOld.txt: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}