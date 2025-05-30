package com.assignment2.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import com.assignment2.model.Item;
import com.assignment2.model.PurchaseOrder;
import com.assignment2.model.Supplier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JsonFileUtil {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Read items from JSON file
    public static List<Item> readItems(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            Type itemListType = new TypeToken<List<Item>>() {
            }.getType();
            return gson.fromJson(reader, itemListType);
        }
    }

    // Read supplier from JSON file
    public static Supplier readSupplier(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, Supplier.class);
        }
    }

    // Save items to JSON file
    public static void saveItems(List<Item> items, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(items, writer);
        }
    }

    // Save supplier to JSON file
    public static void saveSupplier(Supplier supplier, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(supplier, writer);
        }
    }

    public static List<Supplier> readSuppliers(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            Type supplierListType = new TypeToken<List<Supplier>>() {
            }.getType();
            return gson.fromJson(reader, supplierListType);
        }
    }

    // Read purchase orders from JSON file
    public static List<PurchaseOrder> readPurchaseOrders(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            Type purchaseOrderListType = new TypeToken<List<PurchaseOrder>>() {
            }.getType();
            return gson.fromJson(reader, purchaseOrderListType);
        }
    }

    // Save purchase orders to JSON file
    public static void savePurchaseOrders(List<PurchaseOrder> orders, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(orders, writer);
        }
    }

    public static void writeToFile(String filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }
}
