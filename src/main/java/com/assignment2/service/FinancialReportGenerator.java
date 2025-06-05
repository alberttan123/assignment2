package com.assignment2.service;

import com.assignment2.helpers.JsonStorageHelper;
import com.google.gson.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class FinancialReportGenerator {

    private static final String SALES_PATH = "Sales.txt";
    private static final String ITEMS_PATH = "items.txt";
    private static final String REPORT_PATH = "FinancialReport.txt";

    public static void generateReport() {
        try {
            JsonArray salesArray = JsonStorageHelper.loadAsJsonArray(SALES_PATH);
            JsonArray itemsArray = JsonStorageHelper.loadAsJsonArray(ITEMS_PATH);

            Map<String, JsonObject> itemMap = new HashMap<>();
            for (JsonElement itemElem : itemsArray) {
                JsonObject item = itemElem.getAsJsonObject();
                itemMap.put(item.get("itemId").getAsString(), item);
            }

            StringBuilder report = new StringBuilder();
            report.append(String.format("%-15s %-10s %-15s %-15s %-12s %-10s%n",
                    "Item Name", "Qty Sold", "Buying Price", "Total Sales", "Cost", "Profit"));
            report.append("-------------------------------------------------------------------------\n");

            double totalProfit = 0.0;

            for (JsonElement saleElem : salesArray) {
                JsonObject sale = saleElem.getAsJsonObject();
                String itemId = sale.get("itemId").getAsString();
                int quantitySold = Integer.parseInt(sale.get("quantitySold").getAsString());
                double totalSales = Double.parseDouble(sale.get("totalSales").getAsString());

                JsonObject item = itemMap.get(itemId);
                if (item == null) continue;

                String itemName = item.get("itemName").getAsString();
                double buyingPrice = Double.parseDouble(item.get("buyingPrice").getAsString());
                double cost = quantitySold * buyingPrice;
                double profit = totalSales - cost;
                totalProfit += profit;

                report.append(String.format("%-15s %-10d RM %-12.2f RM %-12.2f RM %-10.2f RM %.2f%n",
                        itemName, quantitySold, buyingPrice, totalSales, cost, profit));
            }

            report.append("\nTotal Profit: RM " + String.format("%.2f", totalProfit));

            // Write to file
            try (PrintWriter writer = new PrintWriter(new FileWriter(REPORT_PATH))) {
                writer.write(report.toString());
            }

            // Show success message
            JOptionPane.showMessageDialog(null, "✅ Financial report generated successfully!");

            // Try to open file
            Desktop desktop = Desktop.getDesktop();
            File reportFile = new File(REPORT_PATH);
            if (reportFile.exists()) {
                desktop.open(reportFile);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "❌ Failed to generate report: " + e.getMessage());
            e.printStackTrace();
        }
    }
}