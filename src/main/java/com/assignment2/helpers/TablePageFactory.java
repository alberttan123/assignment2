
package com.assignment2.helpers;

import com.assignment2.gui_albert.FieldDefinition;
import com.assignment2.gui_albert.TablePage;
import com.assignment2.service.UserTableHandler;
import com.assignment2.service.itemsTableHandler;
import com.assignment2.service.poTableHandler;
import com.assignment2.service.supplierTableHandler;
import com.assignment2.gui_xiang.UpdateStockFromApprovedPOs;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class TablePageFactory {

    public static TablePage createUserTable() {
        TablePage tablePage = null;
        try{
            String filePath = "/data/users.txt";
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

    public static TablePage createPOTable() {
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

            tablePage = new TablePage("Purchase Orders", true, false, false, excluded, combined, columnOrder, pointerKeyPath, convertedArray, true);

            tablePage.setTableActionHandler(new poTableHandler(tablePage, false));
            tablePage.setTableActionAdapter(new poTableHandler(tablePage, false));
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("PurchaseOrder.txt not found.");
        }
        return tablePage;
    }

    public static TablePage createPOTablePurchaseManager() {
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

            tablePage = new TablePage("Purchase Orders", true, false, true, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);

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
            System.out.println("/PurchaseOrder.txt not found.");
        }
        return tablePage;
    }

    public static TablePage createViewPOTable() {
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
}
