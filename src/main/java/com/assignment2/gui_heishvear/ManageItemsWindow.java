/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.assignment2.gui_heishvear;
import com.assignment2.gui_xiang.ItemsWindow;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author HeishzXX
 */
public class ManageItemsWindow extends JFrame {
    public ManageItemsWindow() {
        setTitle("Manage Items");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        // Displaying ItemsWindow Page
        ItemsWindow itemsWindow = new ItemsWindow();
        itemsWindow.setVisible(true);
        
        // Create main panel
//        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
//        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }
}
