package com.assignment2.gui_albert;

import com.assignment2.helpers.ComboItem;
import com.assignment2.helpers.EditDialogContext;
import com.assignment2.gui_albert.FieldDefinition;
import com.assignment2.helpers.JsonDropdownLoader;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EditDialog extends JDialog {
    private final Consumer<JsonObject> onSaveCallback;
    private final Map<String, FieldDefinition> fieldDefinitions;
    private final EditDialogContext context;
    private final Map<String, JComponent> inputFields = new HashMap<>();

    public EditDialog(JFrame parent, Consumer<JsonObject> onSaveCallback,
                      Map<String, FieldDefinition> fieldDefinitions, EditDialogContext context) {
        super(parent, "Edit Record", true);
        this.onSaveCallback = onSaveCallback;
        this.fieldDefinitions = fieldDefinitions;
        this.context = context;
        initializeDialog();
    }

    private void initializeDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        for (Map.Entry<String, FieldDefinition> entry : fieldDefinitions.entrySet()) {
            String key = entry.getKey();
            FieldDefinition def = entry.getValue();
            JLabel label = new JLabel(def.label != null ? def.label : key);
            JComponent input;

            switch (def.inputType) {
                case "dropdown":
                    JComboBox<ComboItem> dropdown = new JComboBox<>();
                    JsonDropdownLoader.populateDropdown(dropdown, def);
                    if (context.editedData != null && context.editedData.has(key)) {
                        String currentValue = context.editedData.get(key).getAsString();
                        for (int i = 0; i < dropdown.getItemCount(); i++) {
                            ComboItem item = dropdown.getItemAt(i);
                            if (item.getValue().equals(currentValue)) {
                                dropdown.setSelectedItem(item);
                                break;
                            }
                        }
                    }
                    input = dropdown;
                    break;

                case "int":
                case "float":
                case "string":
                default:
                    JTextField textField = new JTextField();
                    if (context.editedData != null && context.editedData.has(key)) {
                        textField.setText(context.editedData.get(key).getAsString());
                    } else if (def.defaultValue != null) {
                        textField.setText(def.defaultValue);
                    }
                    input = textField;
                    break;
            }

            inputFields.put(def.key, input);
            panel.add(label);
            panel.add(input);
        }

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this::handleSave);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getParent());
    }

    private void handleSave(ActionEvent e) {
        JsonObject newData = new JsonObject();

        for (FieldDefinition def : fieldDefinitions.values()) {
            String label = def.label;
            String jsonKey = def.key;
            JComponent input = inputFields.get(jsonKey); // inputFields was keyed by jsonKey

            String value = null;
            if (input instanceof JComboBox) {
                ComboItem selected = (ComboItem) ((JComboBox<?>) input).getSelectedItem();
                if (selected != null) {
                    value = selected.getValue();
                }
            } else if (input instanceof JTextField) {
                value = ((JTextField) input).getText().trim();
            }

            if (def.required && (value == null || value.isEmpty())) {
                JOptionPane.showMessageDialog(this, (label != null ? label : jsonKey) + " is required.");
                return;
            }

            if (def.regex != null && value != null && !value.matches(def.regex)) {
                JOptionPane.showMessageDialog(this, (label != null ? label : jsonKey) + " is invalid.");
                return;
            }

            newData.addProperty(jsonKey, value);
        }

        onSaveCallback.accept(newData);
        dispose();
    }
}
