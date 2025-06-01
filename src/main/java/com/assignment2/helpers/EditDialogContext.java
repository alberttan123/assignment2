package com.assignment2.helpers;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class EditDialogContext {
    public JsonObject editedData;     // Editable fields (label-based)
    public JsonObject originalData;   // Full raw record (IDs, raw keys)
    public String tableName;          // Optional: for conditional logic
    public Map<String, Object> meta; // Flexible metadata

    public EditDialogContext() {
        this.meta = new HashMap<>();
    }

    public void putMeta(String key, Object value) {
        this.meta.put(key, value);
    }

    public Object getMeta(String key) {
        return this.meta.get(key);
    }
}