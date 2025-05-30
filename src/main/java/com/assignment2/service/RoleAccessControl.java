package com.assignment2.service;

import java.util.*;

public class RoleAccessControl {
    private static final Map<String, Set<String>> roleAccessMap = new HashMap<>();

    static {
        roleAccessMap.put("sales_manager", new HashSet<>(Arrays.asList(
                "manage_items",
                "manage_suppliers",
                "enter_daily_sales",
                "create_pr",
                "view_prs",
                "view_po_list"
        )));

        roleAccessMap.put("inventory_manager", new HashSet<>(Arrays.asList(
                "view_items",
                "update_stock_from_po",
                "generate_stock_reports",
                "track_low_stock_alerts",
                "view_pos"
        )));

        roleAccessMap.put("purchase_manager", new HashSet<>(Arrays.asList(
                "view_items_suppliers",
                "view_prs",
                "generate_po",
                "edit_po",
                "view_po_list"
        )));

        roleAccessMap.put("finance_manager", new HashSet<>(Arrays.asList(
                "approve_pos",
                "modify_po_quantity_supplier",
                "process_payments",
                "generate_financial_reports",
                "view_pr",
                "view_all_po",
                "approve_pos"
        )));

        roleAccessMap.put("admin", new HashSet<>(Arrays.asList(
                "user_management",
                "full_access"
        )));
    }

    public static boolean hasAccess(String role, String functionName) {
        Set<String> allowed = roleAccessMap.get(role.toLowerCase());
        return allowed != null && (allowed.contains("full_access") || allowed.contains(functionName));
    }

    public static Set<String> getFunctions(String role) {
        if (role == null) return Collections.emptySet();
        Set<String> allowed = roleAccessMap.get(role.toLowerCase());
        return allowed != null ? allowed : Collections.emptySet();
    }

    public static boolean isAdmin(String role) {
        return hasAccess(role, "full_access");
    }

    public static Map<String, Set<String>> getRoleFunctionMap() {
        return Collections.unmodifiableMap(roleAccessMap);
    }
}

