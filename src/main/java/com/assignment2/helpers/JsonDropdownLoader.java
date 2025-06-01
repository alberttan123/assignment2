package com.assignment2.helpers;

import com.assignment2.gui_albert.FieldDefinition;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

import javax.swing.*;

public class JsonDropdownLoader {

    public static void populateDropdown(JComboBox<ComboItem> comboBox, FieldDefinition def) {
        comboBox.removeAllItems();

        JsonArray source = null;
        try {
            source = JsonStorageHelper.loadAsJsonArray(def.sourceFile);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("File not found.");
        }

        if (source == null) return;

        for (JsonElement elem : source) {
            JsonObject obj = elem.getAsJsonObject();

            // Optional filter: only include entries where obj[filterKey] == originalRecord[matchField]
            if (def.filterKey != null && def.matchField != null) {
                // You'd likely want to implement this part in future once cross-field filtering is supported
                continue;
            }
            //AHHHHHHHHHHHHHH

            if (obj.has(def.valueField)) {
                String value = obj.get(def.valueField).getAsString();
                String label = obj.get(def.labelField).getAsString();
                comboBox.addItem(new ComboItem(label, value));
            }
        }
    }
}
