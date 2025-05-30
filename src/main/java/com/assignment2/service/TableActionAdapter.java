package com.assignment2.service;

import com.google.gson.JsonObject;

public abstract class TableActionAdapter implements TableActionHandler {
    public void onApprove(JsonObject row) {}
    public void onReject(JsonObject row) {}
}
