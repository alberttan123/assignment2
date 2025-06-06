package com.assignment2.gui_albert;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.*;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.assignment2.helpers.JsonStorageHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AddPage extends JDialog {

private Map<String, String> fieldLabels = new LinkedHashMap<>(); // preserves order
private Map<String, String> dataTypes = new HashMap<>();
private Map<String, Object> fieldOptions = new HashMap<>(); // optional
protected final Map<String, JComponent> fieldInputs = new HashMap<>(); // holds input components

JButton cancelButton = new JButton("Cancel");
JButton saveButton = new JButton("Save");
Container parent = null;

private String filePath = null;
private String primaryKey = null;
private JsonArray addIds = null;
private Map<String, Function<String, Boolean>> validationRules = new HashMap<>();

    public AddPage(JFrame parent, String filePath, 
                LinkedHashMap<String, String> fieldLabels, 
                Map<String, String> dataTypes, 
                Map<String, Object> fieldOptions, 
                Map<String, Function<String, Boolean>> validationRules,
                String primaryKey){
        super(parent, "Add Entry", true); // true = modal
        this.filePath = filePath;
        this.fieldLabels = fieldLabels;
        this.dataTypes = dataTypes;
        this.fieldOptions = fieldOptions;
        this.validationRules = validationRules;
        this.primaryKey = primaryKey;

        render();
        handleEvents();
        this.setVisible(true);
    }

    public AddPage(JFrame parent, String filePath, 
                LinkedHashMap<String, String> fieldLabels, 
                Map<String, String> dataTypes, 
                Map<String, Object> fieldOptions, 
                Map<String, Function<String, Boolean>> validationRules, 
                JsonArray addIds){
        super(parent, "Add Entry", true); // true = modal
        this.filePath = filePath;
        this.fieldLabels = fieldLabels;
        this.dataTypes = dataTypes;
        this.fieldOptions = fieldOptions;
        this.validationRules = validationRules;
        this.addIds = addIds;

        render();
        handleEvents();
        this.setVisible(true);
    }

    public void render() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel dataEntryPanel = new JPanel();
        dataEntryPanel.setLayout(new BoxLayout(dataEntryPanel, BoxLayout.Y_AXIS));

        for (Map.Entry<String, String> entry : fieldLabels.entrySet()) {
            String label = entry.getKey();
            String key = entry.getValue();
            String type = dataTypes.get(key);

            JLabel jLabel = new JLabel(label);
            JComponent inputField;

            switch (type) {
                case "dropdown":
                    JComboBox<String> comboBox = new JComboBox<>();

                    @SuppressWarnings("unchecked")
                    Map<String, String> options = (Map<String, String>) fieldOptions.get(key);

                    if (options != null) {
                        for (String displayName : options.keySet()) {
                            comboBox.addItem(displayName);
                        }
                    }
                    inputField = comboBox;
                    break;

                case "date":
                    DatePickerGroup dateGroup = new DatePickerGroup();
                    inputField = dateGroup;
                    break;

                case "int":
                    JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
                    inputField = spinner;
                    break;
                case "price":
                case "string":
                default:
                    inputField = new JTextField(20);
                    break;
            }

            fieldInputs.put(key, inputField);

            JPanel fieldRow = new JPanel(new BorderLayout());
            fieldRow.add(jLabel, BorderLayout.NORTH);
            fieldRow.add(inputField, BorderLayout.CENTER);
            dataEntryPanel.add(fieldRow);
        }

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        mainPanel.add(dataEntryPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        this.setContentPane(mainPanel);
        this.pack();
        this.setLocationRelativeTo(null);
    }

    public void handleEvents(){
        cancelButton.addActionListener(e -> {
            System.out.println("clicked");
            this.dispose(); // closes the window
        });

        saveButton.addActionListener(e -> {
            onSave(); // collects input and saves to file
        });
    };

    public void updateState(){
        //shouldnt be needed
    };

    public void onSave() {
        JsonObject newEntry = new JsonObject();

        for (Map.Entry<String, JComponent> entry : fieldInputs.entrySet()) {
            String key = entry.getKey();
            JComponent input = entry.getValue();
            String type = dataTypes.get(key);
            String valueStr = null;

            try {
                if (input instanceof JTextField) {
                    valueStr = ((JTextField) input).getText();
                    if ("price".equals(type)) {
                        if (!valueStr.isEmpty()) {
                            try {
                                double priceVal = Double.parseDouble(valueStr);
                                valueStr = String.format("%.2f", priceVal);
                            } catch (NumberFormatException e) {
                                valueStr = "";
                            }
                        } else {
                            valueStr = "";
                        }
                    }
                } else if (input instanceof JComboBox) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> options = (Map<String, String>) fieldOptions.get(key);
                    String selected = (String) ((JComboBox<?>) input).getSelectedItem();
                    valueStr = options.get(selected); // Use value ID
                } else if (input instanceof DatePickerGroup) {
                    valueStr = ((DatePickerGroup) input).getFormattedDate();
                } else if (input instanceof JSpinner) {
                    valueStr = String.valueOf(((JSpinner) input).getValue());
                }

                // VALIDATION (once, for all types)
                if (validationRules.containsKey(key)) {
                    boolean isValid = validationRules.get(key).apply(valueStr);
                    if (!isValid) {
                        JOptionPane.showMessageDialog(this, "Validation failed for: " + key);
                        return;
                    }
                }

                // Save value
                newEntry.addProperty(key, valueStr);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input for: " + key + " (" + type + ")");
                ex.printStackTrace();
                return;
            }
        }

        try {
            if(primaryKey != null && addIds == null){
                // single id mode
                newEntry.addProperty(primaryKey, String.valueOf(JsonStorageHelper.getNextId(filePath, primaryKey)));
            }else if(primaryKey == null && addIds != null){
                // multi id mode
                for (JsonElement el : addIds) {
                    JsonObject def = el.getAsJsonObject();
                    System.out.println("Saving keys: " + def);

                    String key = def.get("primaryKey").getAsString();
                    String saveAs = def.has("saveAsKey") ? def.get("saveAsKey").getAsString() : key;

                    if (def.has("entry")) {
                        // direct value provided
                        JsonElement entryVal = def.get("entry");
                        newEntry.add(saveAs, entryVal);
                    } else {
                        // auto generate id
                        String path = def.get("filePath").getAsString();
                        String nextId = String.valueOf(JsonStorageHelper.getNextId(path, key));
                        newEntry.addProperty(saveAs, nextId);
                    }
                }
            }
            System.out.println("New entry: " + newEntry);
            JsonStorageHelper.updateOrInsert(filePath, newEntry, null); // matching key is null, because it is add, not update
            JOptionPane.showMessageDialog(this, "Saved successfully!");
            this.dispose();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving data");
        }
    }

    private class DatePickerGroup extends JPanel {
        JComboBox<String> dayBox = new JComboBox<>();
        JComboBox<String> monthBox = new JComboBox<>();
        JComboBox<String> yearBox = new JComboBox<>();

        public DatePickerGroup() {
            for (int i = 1; i <= 31; i++) dayBox.addItem(String.format("%02d", i));
            for (int i = 1; i <= 12; i++) monthBox.addItem(String.format("%02d", i));
            for (int i = 1980; i <= 2050; i++) yearBox.addItem(String.valueOf(i));

            // Set today's date
            LocalDate today = LocalDate.now();
            dayBox.setSelectedItem(String.format("%02d", today.getDayOfMonth()));
            monthBox.setSelectedItem(String.format("%02d", today.getMonthValue()));
            yearBox.setSelectedItem(String.valueOf(today.getYear()));

            this.add(dayBox);
            this.add(monthBox);
            this.add(yearBox);
        }

        public String getFormattedDate() {
            return String.format("%s-%s-%s", yearBox.getSelectedItem(), monthBox.getSelectedItem(), dayBox.getSelectedItem());
        }
    }
}