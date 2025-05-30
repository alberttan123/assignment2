package com.assignment2.gui_albert;

import com.google.gson.JsonObject;

public class EditModal extends GUI {
    private int itemID;
    private JsonObject data;

    @Override
    public void render() {
        System.out.println("Rendering Edit Modal...");
    }

    @Override
    public void handleEvents() {
        // Accept and validate edit fields
    }

    @Override
    public void updateState() {}

    public void loadEditData(JsonObject data) {
        this.data = data;
    }

    public JsonObject getEditedData() {
        return this.data;
    }
}
