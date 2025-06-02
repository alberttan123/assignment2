package com.assignment2.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.assignment2.gui_albert.TablePage;
import com.google.gson.JsonObject;

public class StockReportTableHandler implements TableActionHandler{
    private TablePage page;
    private static boolean isApprove;
    private static String filePath = "items.txt";
    private Map<String, JsonObject> originalDataMap = new LinkedHashMap<>();
    
    public StockReportTableHandler(TablePage page, boolean isApprove){
        this.page = page;
    }
}
