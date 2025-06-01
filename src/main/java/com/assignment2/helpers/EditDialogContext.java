package com.assignment2.helpers;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class EditDialogContext {
    public JsonObject editedData;     // Editable fields (label-based)
    public JsonObject originalData;   // Full raw record (IDs, raw keys)
    public String tableName;          // Optional: for conditional logic
    public Map<String, Object> extra; // Flexible metadata

    public EditDialogContext() {
        this.extra = new HashMap<>();
    }

    public void putExtra(String key, Object value) {
        this.extra.put(key, value);
    }

    public Object getExtra(String key) {
        return this.extra.get(key);
    }
}