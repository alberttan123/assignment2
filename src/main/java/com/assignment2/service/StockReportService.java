package com.assignment2.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.assignment2.model.Item;
import com.assignment2.util.JsonFileUtil;

public class StockReportService {

    private static final String REPORT_FILE = "resources/StockReport.txt";

    public static String generateReport() throws IOException {
        List<Item> items = JsonFileUtil.readItems("resources/items.txt");
        StringBuilder report = new StringBuilder();

        // Add header
        report.append("Stock Report\n");
        report.append("Generated on: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        // Add summary
        report.append("Summary:\n");
        report.append("Total Items: ").append(items.size()).append("\n");
        report.append("Low Stock Items (< 10): ")
                .append(items.stream().filter(item -> item.getStockLevel() < 10).count())
                .append("\n\n");

        // Add detailed report
        report.append("Detailed Stock Report:\n");
        report.append(String.format("%-10s %-30s %-30s %-15s %-10s\n",
                "ID", "Item Name", "Supplier", "Stock Level", "Price"));
        report.append("-".repeat(95)).append("\n");

        // Add items
        for (Item item : items) {
            report.append(String.format("%-10d %-30s %-30s %-15d $%-9.2f\n",
                    item.getItemId(),
                    truncateString(item.getItemName(), 28),
                    truncateString(item.getSupplier().getName(), 28),
                    item.getStockLevel(),
                    item.getPrice()));
        }

        // Save report to file
        JsonFileUtil.writeToFile(REPORT_FILE, report.toString());

        return report.toString();
    }

    private static String truncateString(String str, int length) {
        if (str.length() <= length) {
            return str;
        }
        return str.substring(0, length - 2) + "..";
    }
}
