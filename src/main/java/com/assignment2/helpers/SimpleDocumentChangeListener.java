package com.assignment2.helpers;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SimpleDocumentChangeListener implements DocumentListener {
    private Runnable onChange;

    public SimpleDocumentChangeListener(Runnable onChange) {
        this.onChange = onChange;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        onChange.run();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        onChange.run();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        onChange.run();
    }
}

