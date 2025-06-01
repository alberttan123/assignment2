
package com.assignment2.helpers;

import com.assignment2.gui_albert.TablePage;
import com.assignment2.service.UserTableHandler;
import com.assignment2.service.itemsTableHandler;
import com.assignment2.service.poTableHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            String pointerKeyPath = null;

            tablePage = new TablePage("Items", true, true, true, excluded, combined, columnOrder, pointerKeyPath, convertedArray, false);

            tablePage.setTableActionHandler(new itemsTableHandler(tablePage, tablePage));
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("items.txt not found.");
        }
        return tablePage;
    }
}
