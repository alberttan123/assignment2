package com.assignment2.helpers;

import com.google.gson.JsonObject;
import com.assignment2.helpers.EditDialogContext;

public interface EditValidator {
    boolean validate(JsonObject newData, EditDialogContext context);
}
