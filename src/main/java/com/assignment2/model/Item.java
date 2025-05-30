package com.assignment2.model;

public class Item {

    private int itemId;
    private String itemName;
    private Supplier supplier;
    private int stockLevel;
    private double price;

    // Default constructor for Gson
    public Item() {
    }

    // Constructor with all fields
    public Item(int itemId, String itemName, Supplier supplier, int stockLevel, double price) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.supplier = supplier;
        this.stockLevel = stockLevel;
        this.price = price;
    }

    // Getters and Setters
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public int getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(int stockLevel) {
        this.stockLevel = stockLevel;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Item{"
                + "itemId=" + itemId
                + ", itemName='" + itemName + '\''
                + ", supplier=" + supplier
                + ", stockLevel=" + stockLevel
                + ", price=" + price
                + '}';
    }
}
