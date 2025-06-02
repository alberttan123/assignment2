package com.assignment2.service;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.assignment2.gui_albert.AccountFormPage;
import com.assignment2.gui_albert.TablePage;
import com.assignment2.helpers.JsonStorageHelper;
import com.assignment2.session.SessionManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UserTableHandler implements TableActionHandler {

    private JFrame currentPage;
    private TablePage page;
    private String jsonFilePath = "data/users.txt";

    public UserTableHandler(JFrame currentPage, TablePage page) {
        this.currentPage = currentPage;
        this.page = page;
    }

    @Override
    public void onAdd() {
        currentPage.setVisible(false);
        AccountFormPage form = new AccountFormPage(AccountFormPage.Mode.CREATE, currentPage);
        form.setVisible(true);
    }

    @Override
    public void onEdit(JsonObject rowData) {
        currentPage.setVisible(false);
        String email = rowData.get("email").getAsString();
        AccountFormPage form = new AccountFormPage(AccountFormPage.Mode.EDIT_OTHER, email, currentPage);
        form.setVisible(true);
    }

    @Override
    public void onDelete(JsonObject rowData, String pointerKeyPath) {
        String keyVal = rowData.get("email").getAsString();
        if(!keyVal.equals(SessionManager.getUserEmail())){
            deleteRowFromJson(keyVal, pointerKeyPath);
            System.out.println("Deleted user.");
            page.refreshTableData(getLatestData());
        }else{
            JOptionPane.showMessageDialog(null, "You cannot delete your own account.");
        }
    }

    private JsonArray getLatestData(){
        String jsonFilePath = "data/users.txt";
        JsonObject root = null;
        try {
            root = JsonStorageHelper.loadAsJsonObject(jsonFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root.getAsJsonArray("users");
    }

    private void deleteRowFromJson(String keyValue, String pointerKeyPath) {
        try {
            JsonArray arr = getLatestData();

            for (int i = 0; i < arr.size(); i++) {
                JsonObject obj = arr.get(i).getAsJsonObject();
                String value = getNestedValue(obj, pointerKeyPath);
                if (value.equals(keyValue)) {
                    arr.remove(i);
                    JsonObject bruh = new JsonObject();
                    bruh.add("users", arr);
                    JsonStorageHelper.saveToJson(jsonFilePath, bruh);
                    break;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(currentPage, "Failed to delete row from JSON.");
            e.printStackTrace();
        }
    }

    private String getNestedValue(JsonObject obj, String path) {
        String[] parts = path.split("\\.");
        JsonElement current = obj;

        for (String part : parts) {
            if (current != null && current.isJsonObject()) {
                current = current.getAsJsonObject().get(part);
            } else {
                return "";
            }
        }

        return current != null && !current.isJsonNull() ? current.getAsString() : "";
    }
}

