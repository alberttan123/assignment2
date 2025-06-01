package com.assignment2.gui_albert;

public class FieldDefinition {
    public String inputType;
    public String sourceFile;
    public String labelField;
    public String valueField;
    public String filterKey;
    public String matchField;
    public String key;

    public String label;        // For UI display
    public boolean required;    // Required field toggle
    public String defaultValue; // Default value
    public String regex;        // Regex validation pattern

    public static FieldDefinition of(String type) {
        FieldDefinition def = new FieldDefinition();
        def.inputType = type;
        return def;
    }

    public static FieldDefinition dropdown(String sourceFile, String labelField, String valueField) {
        FieldDefinition def = new FieldDefinition();
        def.inputType = "dropdown";
        def.sourceFile = sourceFile;
        def.labelField = labelField;
        def.valueField = valueField;
        return def;
    }

    public FieldDefinition withKey(String key) {
        this.key = key;
        return this;
    }

    public FieldDefinition withFilter(String filterKey, String matchField) {
        this.filterKey = filterKey;
        this.matchField = matchField;
        return this;
    }

    // optional chainable methods
    public FieldDefinition withLabel(String label) {
        this.label = label;
        return this;
    }

    public FieldDefinition required() {
        this.required = true;
        return this;
    }

    public FieldDefinition withDefault(String value) {
        this.defaultValue = value;
        return this;
    }

    public FieldDefinition withValidation(String regex) {
        this.regex = regex;
        return this;
    }
}
