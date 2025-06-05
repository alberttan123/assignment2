
package com.assignment2.gui_albert;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.assignment2.service.TableActionAdapter;
import com.assignment2.service.TableActionHandler;
import com.assignment2.helpers.JsonStorageHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TablePage extends GUI {
    private String pageTitle;
    private JsonArray jsonData;
    private boolean allowEdit;
    private boolean allowDelete;
    private boolean allowAdd;
    private String[] excludedKeys = new String[0];
    private String[] extractedHeaders;
    private String pointerKeyPath; // e.g., "email" or "user.email"
    private String jsonFilePath;   // e.g., "data/users.txt"
    private TableActionHandler actionHandler;
    private TableActionAdapter actionAdapter;


    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton approveButton;
    private JButton rejectButton;
    private TableRowSorter<TableModel> sorter;
    private JTextField searchField;
    private static List<String> columnOrder = new ArrayList<>();
    private boolean allowApproveReject;
    private JPanel searchPanel;
    private JPanel topPanel;         
    private JPanel customTopPanel;   

    
    private Map<String, String> combinedColumns = new LinkedHashMap<>();
    private Map<String, Function<JsonObject, String>> customFormatters = new HashMap<>();

    public TablePage(String pageTitle, boolean allowEdit, boolean allowDelete, boolean allowAdd, String pointerKeyPath, JsonArray jsonData) {
        this(pageTitle, allowEdit, allowDelete, allowAdd, new String[0], new LinkedHashMap<>(), columnOrder, pointerKeyPath, jsonData, false); // default approve/reject to false
    }

    public TablePage(String pageTitle, boolean allowEdit, boolean allowDelete, boolean allowAdd,
                    String[] excludedKeys, Map<String, String> combinedColumns, List<String> columnOrder,
                    String pointerKeyPath, JsonArray jsonData, boolean allowApproveReject) {

        this.pageTitle = pageTitle;
        this.allowEdit = allowEdit;
        this.allowDelete = allowDelete;
        this.allowAdd = allowAdd;
        this.excludedKeys = excludedKeys;
        this.combinedColumns = combinedColumns;
        TablePage.columnOrder = columnOrder;
        this.pointerKeyPath = pointerKeyPath;
        this.allowApproveReject = allowApproveReject;
        this.jsonData = jsonData;

        this.windowWidth = 1000;
        this.windowHeight = 600;

        initWindow(pageTitle);
        render();
        handleEvents();
        setVisible(true);
    }

    public String getPointerKeyPath(){
        return pointerKeyPath;
    }
    public void setPointerKeyPath(String pointerKeyPath){
        this.pointerKeyPath = pointerKeyPath;
    }
    public void setTableActionHandler(TableActionHandler actionHandler){
        this.actionHandler = actionHandler;
    }

    public void setTableActionAdapter(TableActionAdapter actionAdapter){
        this.actionAdapter = actionAdapter;
    }

    public void setCombinedColumn(String columnName, String spaceSeparatedKeys) {
        combinedColumns.put(columnName, spaceSeparatedKeys);
    }

    public void setFormatter(String columnName, Function<JsonObject, String> formatter) {
        customFormatters.put(columnName, formatter);
    }

    public void refreshTableData(JsonArray newData) {
        this.jsonData = newData;
        this.extractedHeaders = extractHeadersWithFlattening(jsonData);
        Object[][] tableData = parseJsonArrayToTableData(jsonData, extractedHeaders);
        tableModel.setDataVector(tableData, extractedHeaders);
    }

    public void addToTop(Component... components) {
        if (customTopPanel != null) {
            for (Component extra : components) {
                customTopPanel.add(extra);
            }
            customTopPanel.setVisible(true);
            customTopPanel.revalidate();
            customTopPanel.repaint();
        }
    }

    public String getSelectedPointerValue() {
        int row = table.getSelectedRow();
        if (row < 0 || pointerKeyPath == null) return null;
        int modelRow = table.convertRowIndexToModel(row);
        int keyIndex = getColumnIndex("Pr Id");
        if (keyIndex < 0) return null;
        Object val = table.getModel().getValueAt(modelRow, keyIndex);
        return val != null ? val.toString() : null;
    }

    public int getColumnIndex(String key) {
        for (int i = 0; i < extractedHeaders.length; i++) {
            if (extractedHeaders[i].equalsIgnoreCase(key)) {
                return i;
            }
        }
        return -1;
    }

    public JsonObject findRowByPointerValue(String pointerValue) {
        for (JsonElement element : jsonData) {
            JsonObject obj = element.getAsJsonObject();
            String currentValue = getNestedValue(obj, pointerKeyPath);
            if (pointerValue.equals(currentValue)) {
                return obj;
            }
        }
        return null;
    }

    @Override
    public void render() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel layout (vertical stacking of search + custom filters)
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // Search bar row (horizontal)
        searchPanel = new JPanel(new BorderLayout(10, 5));
        searchField = new JTextField();
        searchField.setToolTipText("Search...");
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField);

        // Custom filter row (centered)
        customTopPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        customTopPanel.setVisible(false); // shown only if something is injected

        // Add rows to topPanel
        topPanel.add(searchPanel);
        topPanel.add(customTopPanel);

        // Add topPanel to mainPanel
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Table setup
        extractedHeaders = extractHeadersWithFlattening(jsonData);
        Object[][] tableData = parseJsonArrayToTableData(jsonData, extractedHeaders);

        tableModel = new DefaultTableModel(tableData, extractedHeaders);
        table = new JTable(tableModel);
        table.setDefaultEditor(Object.class, null); // no inline editing

        // Capitalize headers
        JTableHeader tableHeader = table.getTableHeader();
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            String original = columnModel.getColumn(i).getHeaderValue().toString();
            columnModel.getColumn(i).setHeaderValue(capitalizeWords(original));
        }
        tableHeader.repaint();

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom action buttons
        JPanel buttonPanel = new JPanel();
        if (allowAdd) {
            addButton = new JButton("Add");
            buttonPanel.add(addButton);
        }
        if (allowEdit) {
            editButton = new JButton("Edit");
            buttonPanel.add(editButton);
        }
        if (allowDelete) {
            deleteButton = new JButton("Delete");
            buttonPanel.add(deleteButton);
        }
        if (allowApproveReject) {
            approveButton = new JButton("Approve");
            rejectButton = new JButton("Reject");
            buttonPanel.add(approveButton);
            buttonPanel.add(rejectButton);
        }

        JButton exportButton = new JButton("Export to CSV");
        exportButton.addActionListener(e -> exportToCSV());
        buttonPanel.add(exportButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Finally add main panel
        add(mainPanel);
    }

    @Override
    public void handleEvents() {
        if (allowEdit && editButton != null) {
            editButton.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    JsonObject rowData = jsonData.get(modelRow).getAsJsonObject();

                    if (actionHandler != null){
                        actionHandler.onEdit(rowData);
                    }
                }
            });
        }

        if (allowDelete && deleteButton != null) {
            deleteButton.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    JsonObject rowData = jsonData.get(modelRow).getAsJsonObject();

                    if (actionHandler != null){
                        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this row?");
                        if (confirm == JOptionPane.YES_OPTION) {
                            actionHandler.onDelete(rowData, pointerKeyPath);
                        }
                    }
                }
            });
        }

        if (allowAdd && addButton != null) {
            addButton.addActionListener(e -> {
                    if (actionHandler != null){
                        actionHandler.onAdd();
                    }
            });
        }
        
        if (allowApproveReject && approveButton != null && rejectButton != null) {
            approveButton.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (actionAdapter != null){
                    int modelRow = table.convertRowIndexToModel(row);
                    JsonObject rowData = jsonData.get(modelRow).getAsJsonObject();
                    actionAdapter.onApprove(rowData);
                    refreshTableData(jsonData);
                }
            });
            rejectButton.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (actionAdapter != null){
                    int modelRow = table.convertRowIndexToModel(row);
                    JsonObject rowData = jsonData.get(modelRow).getAsJsonObject();
                    actionAdapter.onReject(rowData);
                    refreshTableData(jsonData);
                }
            });
        }

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }

            private void applyFilter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text)); // case-insensitive
                }
            }
        });
    }

    @Override
    public void updateState() {
        table.revalidate();
        table.repaint();
    }

    private Object[][] parseJsonArrayToTableData(JsonArray jsonArray, String[] keys) {
        Object[][] result = new Object[jsonArray.size()][keys.length];

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject obj = jsonArray.get(i).getAsJsonObject();

            for (int j = 0; j < keys.length; j++) {
                String column = keys[j];
                if (customFormatters.containsKey(column)) {
                    result[i][j] = customFormatters.get(column).apply(obj); // formatter wins
                } else if (combinedColumns.containsKey(column)) {
                    String keyGroup = combinedColumns.get(column);

                    // Hardcoded formatter: Birthdate = dd/MM/yyyy
                    if (column.equalsIgnoreCase("Birthdate") && keyGroup.contains("dob")) {
                        try {
                            String day = getNestedValue(obj, "dob.day");
                            String month = getNestedValue(obj, "dob.month");
                            String year = getNestedValue(obj, "dob.year");
                            result[i][j] = String.format("%02d/%02d/%04d",
                                Integer.parseInt(day), Integer.parseInt(month), Integer.parseInt(year));
                        } catch (Exception e) {
                            result[i][j] = "-";
                        }
                    } else {
                        // Default combiner fallback
                        StringBuilder sb = new StringBuilder();
                        for (String part : keyGroup.split(" ")) {
                            sb.append(getNestedValue(obj, part)).append(" ");
                        }
                        result[i][j] = sb.toString().trim();
                    }
                } else {
                    result[i][j] = getNestedValue(obj, column);
                }
            }
        }

        return result;
    }

    private String[] extractHeadersWithFlattening(JsonArray jsonArray) {
        Set<String> headers = new LinkedHashSet<>();

        for (JsonElement elem : jsonArray) {
            if (elem.isJsonObject()) {
                flattenJsonKeys(elem.getAsJsonObject(), "", headers);
            }
        }

        headers.removeAll(List.of(excludedKeys));
        headers.addAll(combinedColumns.keySet());

        if (!columnOrder.isEmpty()) {
            List<String> finalHeaders = new ArrayList<>();
            for (String h : columnOrder) {
                if (headers.contains(h)) {
                    finalHeaders.add(h);
                }
            }
            return finalHeaders.toArray(new String[0]);
        } else {
            return headers.toArray(new String[0]);
        }
    }

    private void flattenJsonKeys(JsonObject obj, String prefix, Set<String> keys) {
        for (String key : obj.keySet()) {
            JsonElement val = obj.get(key);
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (val.isJsonObject()) {
                flattenJsonKeys(val.getAsJsonObject(), fullKey, keys);
            } else {
                keys.add(fullKey);
            }
        }
    }

    private String getNestedValue(JsonObject obj, String path) {
        String[] parts = path.split("\\.");
        JsonElement current = obj;

        for (String part : parts) {
            if (current != null && current.isJsonObject()) {
                current = current.getAsJsonObject().get(part);
            } else {
                return "";
            }
        }

        return current != null && !current.isJsonNull() ? current.getAsString() : "";
    }

    private void exportToCSV() {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                sb.append(tableModel.getColumnName(i)).append(",");
            }
            sb.append("\n");

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    sb.append(tableModel.getValueAt(row, col)).append(",");
                }
                sb.append("\n");
            }

            java.nio.file.Files.write(java.nio.file.Path.of("export.csv"), sb.toString().getBytes());
            JOptionPane.showMessageDialog(this, "Exported to export.csv");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed.");
            e.printStackTrace();
        }
    }

    private String capitalizeWords(String input) {
        String withSpaces = input.replaceAll("([a-z])([A-Z])", "$1 $2")
                                .replaceAll("([A-Z])([A-Z][a-z])", "$1 $2");

        String[] words = withSpaces.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                .append(word.substring(1).toLowerCase())
                .append(" ");
            }
        }
        return sb.toString().trim();
    }
    public JsonObject getSelectedRowData() {
        JsonObject returnArray = null;
        int selectedViewRow = table.getSelectedRow();

        int modelrow = table.convertRowIndexToModel(selectedViewRow);
        for (int i = 0; i< tableModel.getRowCount(); i++){
            returnArray.addProperty("Pr Id", (String) tableModel.getValueAt(modelrow, 0));
            returnArray.addProperty("Item", (String) tableModel.getValueAt(modelrow, 1));
            returnArray.addProperty("Quantity", (String) tableModel.getValueAt(modelrow, 2));
            returnArray.addProperty("Supplier", (String) tableModel.getValueAt(modelrow, 3));
            returnArray.addProperty("Required By", (String) tableModel.getValueAt(modelrow, 4));
            returnArray.addProperty("Raised By", (String) tableModel.getValueAt(modelrow, 5));
        }

        return returnArray;
    }
}
