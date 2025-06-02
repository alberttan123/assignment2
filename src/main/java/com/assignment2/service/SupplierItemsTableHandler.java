package com.assignment2.service;

public class SupplierItemsTableHandler extends TableActionHandler{
    private JFrame currentPage;
    private TablePage page;
    private String filePath = "supplier_items.txt";
    private Map<String, JsonObject> originalDataMap = new LinkedHashMap<>();

    public SupplierItemsTableHandler(JFrame currentPage, TablePage page) {
        this.currentPage = currentPage;
        this.page = page;
    }

    @Override
    public void onAdd(){
        throw new UnsupportedOperationException("onAdd unused");
    }





    
    public static JsonArray convert(JsonArray rawArray){
        // Initialize a new array to hold the converted objects
        JsonArray convertedArray = new JsonArray();

        for (JsonElement el : rawArray) {
            JsonObject original = el.getAsJsonObject(); // Original JSON object
            JsonObject converted = new JsonObject();    // New object to store converted data
            System.out.println(original);

            // Lookup and add item details based on itemId
            converted.addProperty("supplierId", original.get("supplierId").getAsInt());
            converted.addProperty("Supplier", getNameById("Supplier.txt", "supplierId", original.get("supplierId").getAsInt(), "name"));
            converted.addProperty("Address", getNameById("Supplier.txt", "supplierId", original.get("supplierId").getAsInt(), "address"));

            // Add the converted object to the result array
            convertedArray.add(converted);
        }
        return convertedArray;
    } 
}
