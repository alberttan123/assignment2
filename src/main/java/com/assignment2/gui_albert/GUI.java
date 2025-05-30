package com.assignment2.gui_albert;

// Base imports used across GUI classes
import javax.swing.JFrame;

// Abstract GUI base class
public abstract class GUI extends JFrame{
    protected int windowWidth;
    protected int windowHeight;

    public abstract void render();
    public abstract void handleEvents();
    public abstract void updateState();

    public void resizeWindow(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
        setSize(width, height);
    }

    public void initWindow(String title){
        setTitle(title);
        setSize(windowWidth, windowHeight);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
