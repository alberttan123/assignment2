package com.assignment2.gui_xiang;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.assignment2.model.Item;

public class EditItemDialog extends JDialog {

    private boolean confirmed = false;
    private final Item item;
    private JTextField nameField;
    private JTextField stockField;
    private JTextField priceField;

    public EditItemDialog(JFrame parent, Item item) {
        super(parent, "Edit Item", true);
        this.item = item;

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add ID label (non-editable)
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Item ID:"), gbc);
        gbc.gridx = 1;
        JTextField idField = new JTextField(String.valueOf(item.getItemId()));
        idField.setEditable(false);
        idField.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(idField, gbc);

        // Add name field
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(item.getItemName());
        nameField.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(nameField, gbc);

        // Add supplier label (non-editable)
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1;
        JTextField supplierField = new JTextField(item.getSupplier().getName());
        supplierField.setEditable(false);
        supplierField.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(supplierField, gbc);

        // Add stock level field
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Stock Level:"), gbc);
        gbc.gridx = 1;
        stockField = new JTextField(String.valueOf(item.getStockLevel()));
        stockField.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(stockField, gbc);

        // Add price field
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        priceField = new JTextField(String.format("%.2f", item.getPrice()));
        priceField.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(priceField, gbc);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add button panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);

        // Add main panel to dialog
        add(mainPanel);
        pack();
        setLocationRelativeTo(parent);
    }

    private boolean validateInput() {
        try {
            // Validate name
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showError("Item name cannot be empty");
                return false;
            }

            // Validate stock level
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) {
                showError("Stock level cannot be negative");
                return false;
            }

            // Validate price
            double price = Double.parseDouble(priceField.getText().trim());
            if (price < 0) {
                showError("Price cannot be negative");
                return false;
            }

            // Update item if validation passes
            item.setItemName(name);
            item.setStockLevel(stock);
            item.setPrice(price);

            return true;
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for stock level and price");
            return false;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Item getItem() {
        return item;
    }
}
