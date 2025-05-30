package com.assignment2.gui_albert;

import com.google.gson.JsonObject;

public class DataEntryPage extends GUI {
    private JsonObject data;

    @Override
    public void render() {
        System.out.println("Rendering Data Entry Page...");
    }

    @Override
    public void handleEvents() {
        // Handle form inputs and submission
    }

    @Override
    public void updateState() {}

    public void populateFields(JsonObject data) {
        this.data = data;
    }

    public JsonObject collectFormData() {
        return this.data;
    }
}
