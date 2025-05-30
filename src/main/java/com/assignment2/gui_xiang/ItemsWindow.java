package com.assignment2.gui_xiang;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.assignment2.model.Item;
import com.assignment2.util.JsonFileUtil;

public class ItemsWindow extends JFrame {

    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private List<Item> items;
    private List<Item> filteredItems;
    private JTextField searchField;

    public ItemsWindow() {
        setTitle("View Items");
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create top control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton filterBtn = createStyledButton("Filter");
        searchField = new JTextField(30);
        searchField.setPreferredSize(new Dimension(200, 30));
        JButton searchBtn = createStyledButton("Search");
        JButton showAllBtn = createStyledButton("Show All");

        // Add action listeners for search functionality
        searchBtn.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch()); // Allow search on Enter key
        showAllBtn.addActionListener(e -> showAllItems());

        controlPanel.add(filterBtn);
        controlPanel.add(searchField);
        controlPanel.add(searchBtn);
        controlPanel.add(showAllBtn);

        // Create table
        String[] columnNames = {"Item ID", "Item Name", "Supplier", "Stock Level", "Price", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only allow editing in the Actions column
            }
        };

        itemsTable = new JTable(tableModel);
        itemsTable.setRowHeight(35); // Increase row height
        itemsTable.setIntercellSpacing(new Dimension(10, 10)); // Add spacing between cells
        itemsTable.setShowGrid(true);
        itemsTable.setGridColor(Color.LIGHT_GRAY);
        itemsTable.getTableHeader().setPreferredSize(new Dimension(100, 35)); // Increase header height
        itemsTable.getTableHeader().setBackground(new Color(240, 240, 240));
        itemsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        itemsTable.setFont(new Font("Arial", Font.PLAIN, 12));

        // Center align the header text
        ((DefaultTableCellRenderer) itemsTable.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(JLabel.CENTER);

        // Center align all columns except Item Name and Supplier
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < itemsTable.getColumnCount(); i++) {
            if (i != 1 && i != 2) { // Skip Item Name and Supplier columns
                itemsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // Set custom renderer for the Actions column
        itemsTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        itemsTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), this));

        // Set column widths
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Name
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Supplier
        itemsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Stock
        itemsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Price
        itemsTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Actions

        // Create pagination panel
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton prevBtn = new JButton("←");
        JLabel pageLabel = new JLabel("Page 1 of 1");
        JButton nextBtn = new JButton("→");
        paginationPanel.add(prevBtn);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextBtn);

        // Add components to main panel
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        mainPanel.add(paginationPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);

        // Load items
        loadItems();
    }

    private void performSearch() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            showAllItems();
            return;
        }

        filteredItems = items.stream()
                .filter(item
                        -> String.valueOf(item.getItemId()).contains(searchText)
                || item.getItemName().toLowerCase().contains(searchText)
                || item.getSupplier().getName().toLowerCase().contains(searchText)
                || String.valueOf(item.getStockLevel()).contains(searchText)
                || String.format("%.2f", item.getPrice()).contains(searchText))
                .collect(Collectors.toList());

        updateTableWithItems(filteredItems);
    }

    private void showAllItems() {
        searchField.setText("");
        filteredItems = new ArrayList<>(items);
        updateTableWithItems(items);
    }

    private void updateTableWithItems(List<Item> itemsToShow) {
        tableModel.setRowCount(0); // Clear existing rows
        for (Item item : itemsToShow) {
            tableModel.addRow(new Object[]{
                item.getItemId(),
                item.getItemName(),
                item.getSupplier().getName(),
                item.getStockLevel(),
                String.format("$%.2f", item.getPrice()),
                "Actions" // This will be replaced by buttons in the renderer
            });
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(100, 30));
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setFocusPainted(false);
        return button;
    }

    private void loadItems() {
        try {
            items = JsonFileUtil.readItems("resources/items.txt");
            filteredItems = new ArrayList<>(items); // Initialize filtered items with all items
            updateTableWithItems(items);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading items: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public Item getItemAt(int row) {
        if (row >= 0 && row < filteredItems.size()) {
            return filteredItems.get(row);
        }
        return null;
    }

    public List<Item> getAllItems() {
        return items;
    }

    public void refreshTable() {
        updateTableWithItems(filteredItems);
    }
}

// Custom renderer for the buttons column
class ButtonRenderer extends JPanel implements TableCellRenderer {

    private JButton editBtn;

    public ButtonRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        editBtn = new JButton("Edit");

        // Style the button
        editBtn.setPreferredSize(new Dimension(80, 25));
        editBtn.setFont(new Font("Arial", Font.PLAIN, 12));

        add(editBtn);
        setBackground(null); // Make panel transparent
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
}

// Custom editor for the buttons column
class ButtonEditor extends DefaultCellEditor {

    private JPanel panel;
    private JButton editBtn;
    private final ItemsWindow parent;

    public ButtonEditor(JCheckBox checkBox, ItemsWindow parent) {
        super(checkBox);
        this.parent = parent;
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        editBtn = new JButton("Edit");

        // Style the button
        editBtn.setPreferredSize(new Dimension(80, 25));
        editBtn.setFont(new Font("Arial", Font.PLAIN, 12));

        editBtn.addActionListener(e -> {
            int row = (Integer) editBtn.getClientProperty("row");
            Item item = parent.getItemAt(row);
            if (item != null) {
                EditItemDialog dialog = new EditItemDialog(parent, item);
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    try {
                        // Save changes to file
                        JsonFileUtil.saveItems(parent.getAllItems(), "resources/items.txt");
                        // Update the table
                        parent.refreshTable();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(parent,
                                "Error saving changes: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        panel.add(editBtn);
        panel.setBackground(null); // Make panel transparent
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        editBtn.putClientProperty("row", row);
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return "Actions";
    }
}
