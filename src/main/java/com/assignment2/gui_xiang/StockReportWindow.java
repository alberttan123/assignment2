package com.assignment2.gui_xiang;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.assignment2.service.StockReportService;

public class StockReportWindow extends JFrame {

    private JTextArea reportArea;

    public StockReportWindow() {
        setTitle("Stock Report");
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create report text area
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton generateBtn = new JButton("Generate New Report");
        generateBtn.setPreferredSize(new Dimension(150, 30));
        generateBtn.addActionListener(e -> generateReport());
        buttonPanel.add(generateBtn);

        // Add components to main panel
        mainPanel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);

        // Generate initial report
        generateReport();
    }

    private void generateReport() {
        try {
            String report = StockReportService.generateReport();
            reportArea.setText(report);
            reportArea.setCaretPosition(0); // Scroll to top

            JOptionPane.showMessageDialog(this,
                    "Stock report has been generated and saved to StockReport.txt",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error generating report: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
