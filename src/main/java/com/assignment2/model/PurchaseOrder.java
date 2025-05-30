package com.assignment2.model;

public class PurchaseOrder {

    private int poId;
    private int prId;
    private int itemId;
    private int supplierId;
    private int quantity;
    private String status;
    private int generatedByUserId;
    private int approvedByUserId;

    // Default constructor for Gson
    public PurchaseOrder() {
    }

    // Constructor with all fields
    public PurchaseOrder(int poId, int prId, int itemId, int supplierId, int quantity,
            String status, int generatedByUserId, int approvedByUserId) {
        this.poId = poId;
        this.prId = prId;
        this.itemId = itemId;
        this.supplierId = supplierId;
        this.quantity = quantity;
        this.status = status;
        this.generatedByUserId = generatedByUserId;
        this.approvedByUserId = approvedByUserId;
    }

    // Getters and Setters
    public int getPoId() {
        return poId;
    }

    public void setPoId(int poId) {
        this.poId = poId;
    }

    public int getPrId() {
        return prId;
    }

    public void setPrId(int prId) {
        this.prId = prId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getGeneratedByUserId() {
        return generatedByUserId;
    }

    public void setGeneratedByUserId(int generatedByUserId) {
        this.generatedByUserId = generatedByUserId;
    }

    public int getApprovedByUserId() {
        return approvedByUserId;
    }

    public void setApprovedByUserId(int approvedByUserId) {
        this.approvedByUserId = approvedByUserId;
    }

    @Override
    public String toString() {
        return "PurchaseOrder{"
                + "poId=" + poId
                + ", prId=" + prId
                + ", itemId=" + itemId
                + ", supplierId=" + supplierId
                + ", quantity=" + quantity
                + ", status='" + status + '\''
                + ", generatedByUserId=" + generatedByUserId
                + ", approvedByUserId=" + approvedByUserId
                + '}';
    }
}
