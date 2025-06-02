package com.assignment2.helpers;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.assignment2.gui_albert.HomePage;
import com.assignment2.gui_albert.TablePage;

public class FunctionButtonFactory {

    public static Map<String, JButton> createFunctionButtons(JFrame currentWindow) {
        Map<String, JButton> buttons = new HashMap<>();
        Map<String, Runnable> actions = new HashMap<>();

        actions.put("manage_items", () -> {
            System.out.println("Opening Item Management Window...");
            if (currentWindow != null) currentWindow.dispose();

            TablePage poTable = TablePageFactory.createItemsTable();
            if (poTable != null) {
                poTable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                poTable.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        System.out.println("Returning to Home Page...");
                        new com.assignment2.gui_albert.HomePage().setVisible(true);
                    }
                });
                poTable.setVisible(true);
            }
        });

        actions.put("manage_suppliers", () -> {
            System.out.println("Opening Supplier Management Window...");
            if (currentWindow != null) currentWindow.dispose();

            TablePage poTable = TablePageFactory.createSupplierTable();
            if (poTable != null) {
                poTable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                poTable.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        System.out.println("Returning to Home Page...");
                        new com.assignment2.gui_albert.HomePage().setVisible(true);
                    }
                });
                poTable.setVisible(true);
            }
        });

        actions.put("enter_daily_sales", () -> {
            System.out.println("Launching Daily Sales Entry...");
        });

        actions.put("create_pr", () -> {
            System.out.println("Launching Create PR Dialog...");
        });

        actions.put("view_prs", () -> {
            System.out.println("Displaying PR List...");
        });

        actions.put("view_po_list", () -> {
            System.out.println("Showing PO List...");
            if (currentWindow != null) currentWindow.dispose();

            TablePage poTable = TablePageFactory.createPOTablePurchaseManager();
            if (poTable != null) {
                poTable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                poTable.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        System.out.println("Returning to Home Page...");
                        new com.assignment2.gui_albert.HomePage().setVisible(true);
                    }
                });
                poTable.setVisible(true);
            }
        });

        actions.put("view_items", () -> {
            System.out.println("Viewing Items...");
            if (currentWindow != null) currentWindow.dispose();

            TablePage poTable = TablePageFactory.createViewItemsTable();
            if (poTable != null) {
                poTable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                poTable.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        System.out.println("Returning to Home Page...");
                        new com.assignment2.gui_albert.HomePage().setVisible(true);
                    }
                });
                poTable.setVisible(true);
            }
        });

        actions.put("update_stock_from_po", () -> {
            System.out.println("Updating Stock from PO...");
            if (currentWindow != null) currentWindow.dispose();

            TablePage poTable = TablePageFactory.createUpdateStockTable();
            if (poTable != null) {
                poTable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                poTable.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        System.out.println("Returning to Home Page...");
                        new com.assignment2.gui_albert.HomePage().setVisible(true);
                    }
                });
                poTable.setVisible(true);
            }
        });

        actions.put("generate_stock_reports", () -> {
            System.out.println("Generating Stock Reports...");
            if (currentWindow != null) currentWindow.dispose();

            TablePage poTable = TablePageFactory.createStockReportTable();
            if (poTable != null) {
                poTable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                poTable.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        System.out.println("Returning to Home Page...");
                        new com.assignment2.gui_albert.HomePage().setVisible(true);
                    }
                });
                poTable.setVisible(true);
            }
        });

        actions.put("track_low_stock_alerts", () -> {
            System.out.println("Tracking Low Stock Alerts...");
            if (currentWindow != null) currentWindow.dispose();

            TablePage poTable = TablePageFactory.createLowStockTable();
            if (poTable != null) {
                poTable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                poTable.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        System.out.println("Returning to Home Page...");
                        new com.assignment2.gui_albert.HomePage().setVisible(true);
                    }
                });
                poTable.setVisible(true);
            }
        });

        actions.put("view_pos", () -> {
            System.out.println("Viewing Purchase Orders...");
                        if (currentWindow != null) currentWindow.dispose();

            TablePage poTable = TablePageFactory.createViewPOTable();
            if (poTable != null) {
                poTable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                poTable.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        System.out.println("Returning to Home Page...");
                        new com.assignment2.gui_albert.HomePage().setVisible(true);
                    }
                });
                poTable.setVisible(true);
            }
        });

        actions.put("view_items_suppliers", () -> {
            System.out.println("Viewing Items and Suppliers...");
        });

        actions.put("process_payments", () -> {
            System.out.println("Processing Payments...");
        });

        actions.put("generate_financial_reports", () -> {
            System.out.println("Generating Financial Reports...");
        });

        actions.put("view_pr", () -> {
            System.out.println("Viewing Purchase Requisition...");
        });

        actions.put("view_all_po", () -> {
            System.out.println("Viewing Purchase Order...");
            if (currentWindow != null) currentWindow.dispose();

            TablePage poTable = TablePageFactory.createPOTable();
            if (poTable != null) {
                poTable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                poTable.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        System.out.println("Returning to Home Page...");
                        new com.assignment2.gui_albert.HomePage().setVisible(true);
                    }
                });
                poTable.setVisible(true);
            }
        });

        actions.put("approve_pos", () -> {
            System.out.println("Viewing Approve Purchase Order...");
            if (currentWindow != null) currentWindow.dispose();

            TablePage poTable = TablePageFactory.createApprovePOTable();
            if (poTable != null) {
                poTable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                poTable.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        System.out.println("Returning to Home Page...");
                        new com.assignment2.gui_albert.HomePage().setVisible(true);
                    }
                });
                poTable.setVisible(true);
            }
        });

        actions.put("user_management", () -> {
            System.out.println("Opening User Management Panel...");
            if (currentWindow != null) currentWindow.dispose();

            TablePage userTable = TablePageFactory.createUserTable();
            if (userTable != null) {
                userTable.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                userTable.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        System.out.println("Returning to Home Page...");
                        new com.assignment2.gui_albert.HomePage().setVisible(true);
                    }
                });
                userTable.setVisible(true);
            }
        });

        actions.put("main_menu", () -> {
            System.out.println("Returning to Main Menu...");
            if (currentWindow != null) currentWindow.dispose();
            new HomePage().setVisible(true);
        });

        for (Map.Entry<String, Runnable> entry : actions.entrySet()) {
            JButton button = new JButton(toTitle(entry.getKey()));
            button.setPreferredSize(new Dimension(200, 40));
            button.addActionListener(e -> entry.getValue().run());
            buttons.put(entry.getKey(), button);
        }

        return buttons;
    }

    public static JButton getButton(String functionName) {
        return createFunctionButtons(null).get(functionName);
    }

    private static String toTitle(String key) {
        String[] words = key.split("_");
        StringBuilder title = new StringBuilder();

        for (String word : words) {
            if (word.length() == 2) {
                // ALL CAPS for 2-letter words
                title.append(word.toUpperCase()).append(" ");
            } else if (word.length() == 3 && !word.equals("all") && !word.equals("low")) {
                // First 2 letters capitalized for 3-letter words
                title.append(word.substring(0, 2).toUpperCase())
                    .append(word.substring(2)).append(" ");
            } else if (word.length() > 0) {
                // Capitalize first letter for others
                title.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1)).append(" ");
            }
        }

        return title.toString().trim();
    }

    public static void openWithReturnToHome(JFrame currentWindow, Supplier<? extends JFrame> targetPageSupplier) {
        currentWindow.dispose();

        JFrame page = targetPageSupplier.get();
        page.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        page.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                System.out.println("Returning to Home Page...");
                new HomePage().setVisible(true);
            }
        });

        page.setVisible(true);
    }
}
