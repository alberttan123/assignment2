package com.assignment2.service;

import com.google.gson.JsonObject;

public interface TableActionHandler {
    void onAdd();
    void onEdit(JsonObject rowData);
    void onDelete(JsonObject rowData, String pointerKeyPath);
}
