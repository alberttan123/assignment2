package com.assignment2.service;

import com.google.gson.JsonObject;

public class InventoryChange {
    public enum ChangeType {
        NEW, REMOVED, MODIFIED_NAME, MODIFIED_STOCK, MODIFIED_PRICE, MODIFIED_MULTIPLE
    }

    private String itemId;
    private ChangeType type;
    private String oldValue; // e.g., old stock, old name, old price
    private String newValue; // e.g., new stock, new name, new price
    private String itemName; // Current or relevant item name

    public InventoryChange(String itemId, String itemName, ChangeType type, String oldValue, String newValue) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.type = type;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    // Getters
    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public ChangeType getType() { return type; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }

    public String getTypeDescription() {
        switch (type) {
            case NEW: return "New Item Added";
            case REMOVED: return "Item Removed";
            case MODIFIED_NAME: return "Name Changed";
            case MODIFIED_STOCK: return "Stock Level Changed";
            case MODIFIED_PRICE: return "Selling Price Changed";
            case MODIFIED_MULTIPLE: return "Multiple Fields Changed";
            default: return "Unknown Change";
        }
    }

    @Override
    public String toString() {
        return "InventoryChange{" +
                "itemId='" + itemId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", type=" + type +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                '}';
    }
}
