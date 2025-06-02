package com.assignment2.gui_albert;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.assignment2.helpers.JsonStorageHelper;
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

    public AddPage(JFrame parent, String filePath, 
                LinkedHashMap<String, String> fieldLabels, 
                Map<String, String> dataTypes, 
                Map<String, Object> fieldOptions, String primaryKey){
        super(parent, "Add Entry", true); // true = modal
        this.filePath = filePath;
        this.fieldLabels = fieldLabels;
        this.dataTypes = dataTypes;
        this.fieldOptions = fieldOptions;
        this.primaryKey = primaryKey;

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

                case "price":
                case "int":
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

            if (input instanceof JTextField) {
                String value = ((JTextField) input).getText();
                newEntry.addProperty(key, value);
            } else if (input instanceof JComboBox) {
                String selected = (String) ((JComboBox<?>) input).getSelectedItem();

                @SuppressWarnings("unchecked")
                Map<String, String> options = (Map<String, String>) fieldOptions.get(key);

                newEntry.addProperty(key, options.get(selected)); // use value ID
            }
        }

        try {
            newEntry.addProperty(primaryKey, JsonStorageHelper.getNextId(filePath, primaryKey));
            JsonStorageHelper.updateOrInsert(filePath, newEntry, null); // matching key is null, because it is add, not update
            JOptionPane.showMessageDialog(this, "Saved successfully!");
            this.dispose();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving data");
        }
    }
}