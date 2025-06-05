package com.assignment2.service;

import com.assignment2.helpers.JsonStorageHelper;
import com.assignment2.session.SessionManager;
import com.google.gson.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PaymentService {

    private static final String PO_PATH = "PurchaseOrder.txt";
    private static final String ITEMS_PATH = "items.txt";
    private static final String PAYMENTS_PATH = "Payments.txt";

    public static void processPayments() {
        try {
            JsonArray poArray = JsonStorageHelper.loadAsJsonArray(PO_PATH);
            JsonArray itemsArray = JsonStorageHelper.loadAsJsonArray(ITEMS_PATH);
            JsonArray paymentsArray = new JsonArray();
            File paymentsFile = new File(PAYMENTS_PATH);

            if (paymentsFile.exists()) {
                paymentsArray = JsonStorageHelper.loadAsJsonArray(PAYMENTS_PATH);
            }

            Set<Integer> alreadyPaidPoIds = new HashSet<>();
            for (JsonElement payment : paymentsArray) {
                JsonObject p = payment.getAsJsonObject();
                if (p.has("poId")) {
                    alreadyPaidPoIds.add(p.get("poId").getAsInt());
                }
            }

            for (JsonElement poElem : poArray) {
                JsonObject po = poElem.getAsJsonObject();
                if (!"Approved".equals(po.get("status").getAsString())) continue;
                if (!po.has("stockUpdated") || !po.get("stockUpdated").getAsBoolean()) continue;

                int poId = po.get("poId").getAsInt();
                if (alreadyPaidPoIds.contains(poId)) continue;

                int itemId = po.get("itemId").getAsInt();
                int quantity = po.get("quantity").getAsInt();
                int supplierId = po.get("supplierId").getAsInt();
                int paidByUserId = SessionManager.getCurrentUser().get("userId").getAsInt();

                // Find matching item
                JsonObject item = null;
                for (JsonElement itemElem : itemsArray) {
                    JsonObject obj = itemElem.getAsJsonObject();
                    if (obj.get("itemId").getAsString().equals(String.valueOf(itemId))) {
                        item = obj;
                        break;
                    }
                }

                if (item == null || !item.has("buyingPrice")) {
                    System.out.println("Skipping PO " + poId + ": buyingPrice not found.");
                    continue;
                }

                double price = Double.parseDouble(item.get("buyingPrice").getAsString());
                double amount = price * quantity;

                JsonObject payment = new JsonObject();
                payment.addProperty("paymentId", getNextPaymentId(paymentsArray));
                payment.addProperty("poId", poId);
                payment.addProperty("supplierId", supplierId);
                payment.addProperty("amount", String.format("%.2f", amount));
                payment.addProperty("paidByUserId", paidByUserId);
                payment.addProperty("paidAt", getNow());

                // Append
                paymentsArray.add(payment);
            }

            System.out.println(paymentsArray);

            JsonStorageHelper.saveToJson(PAYMENTS_PATH, paymentsArray);
            System.out.println("Payments processed successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getNextPaymentId(JsonArray paymentsArray) {
        int maxId = 0;
        for (JsonElement elem : paymentsArray) {
            JsonObject obj = elem.getAsJsonObject();
            if (obj.has("paymentId")) {
                maxId = Math.max(maxId, obj.get("paymentId").getAsInt());
            }
        }
        return maxId + 1;
    }

    private static String getNow() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
