package com.assignment2.helpers;

public class ComboItem {
    private final String label;
    private final String value;

    public ComboItem(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return label; // this is what shows in the dropdown
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ComboItem) {
            ComboItem other = (ComboItem) obj;
            return value.equals(other.getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
