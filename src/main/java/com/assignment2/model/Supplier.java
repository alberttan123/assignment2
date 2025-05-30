package com.assignment2.model;

import java.util.List;

public class Supplier {

    private int supplierId;
    private String name;
    private List<Item> itemsSupplied;

    // Default constructor for Gson
    public Supplier() {
    }

    // Constructor with basic fields
    public Supplier(int supplierId, String name) {
        this.supplierId = supplierId;
        this.name = name;
    }

    // Constructor with all fields
    public Supplier(int supplierId, String name, List<Item> itemsSupplied) {
        this.supplierId = supplierId;
        this.name = name;
        this.itemsSupplied = itemsSupplied;
    }

    // Getters and Setters
    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Item> getItemsSupplied() {
        return itemsSupplied;
    }

    public void setItemsSupplied(List<Item> itemsSupplied) {
        this.itemsSupplied = itemsSupplied;
    }

    @Override
    public String toString() {
        return "Supplier{"
                + "supplierId=" + supplierId
                + ", name='" + name + '\''
                + ", itemsSupplied=" + itemsSupplied
                + '}';
    }
}
