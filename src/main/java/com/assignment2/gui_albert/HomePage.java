package com.assignment2.gui_albert;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.assignment2.gui_albert.AccountFormPage;
import com.assignment2.gui_albert.AccountFormPage.Mode;
import com.assignment2.gui_xiang.PurchaseOrdersWindow;
import com.assignment2.helpers.FunctionButtonFactory;
import com.assignment2.service.RoleAccessControl;
import com.assignment2.session.SessionManager;

public class HomePage extends GUI {
    private File profilePicturePath;
    private JButton logoutButton;
    private JButton editInfoButton;
    private String userName;

    public HomePage() {
        this.windowWidth = 500;
        this.windowHeight = 300;
        initWindow("Home Page");
        render();
        handleEvents(); 
    }

    @Override
    public void render() {
        System.out.println("Rendering Home Page...");
        userName = SessionManager.getUserEmail();
        String role = SessionManager.getUserRole();
        String fullName = SessionManager.getFullName();

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Left Panel (Profile and Sidebar)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(200, getHeight()));

        JLabel pfpLabel = new JLabel("[PFP]");
        pfpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pfpLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        pfpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if(SessionManager.checkPfpExists()){
            pfpLabel.setText("");
            pfpLabel.setIcon(new ImageIcon(SessionManager.getPfp()));
        }

        JLabel nameLabel = new JLabel(fullName);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel userLabel = new JLabel(userName);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel roleLabel = new JLabel(SessionManager.getGUIUserRole());
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        editInfoButton = new JButton("Edit Personal Info");
        editInfoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton = new JButton("LOG OUT");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(pfpLabel);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(nameLabel);
        leftPanel.add(userLabel);
        leftPanel.add(roleLabel);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(editInfoButton);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(logoutButton);

        // Right Panel (Main Menu Buttons with Grouping)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Map<String, JButton> allButtons = FunctionButtonFactory.createFunctionButtons(this);

        if (RoleAccessControl.isAdmin(role)) {
            Map<String, Set<String>> roleFunctionMap = RoleAccessControl.getRoleFunctionMap();

            for (Map.Entry<String, Set<String>> entry : roleFunctionMap.entrySet()) {
                String roleTitle = entry.getKey().replace("_", " ");
                roleTitle = roleTitle.substring(0, 1).toUpperCase() + roleTitle.substring(1);
                JLabel groupLabel = new JLabel(roleTitle);
                groupLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                groupLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                buttonPanel.add(groupLabel);
                buttonPanel.add(Box.createVerticalStrut(5));

                JPanel groupPanel = new JPanel(new GridLayout(0, 1, 5, 5));
                for (String function : entry.getValue()) {
                    JButton btn = allButtons.get(function);
                    if (btn != null) {
                        groupPanel.add(btn);
                    }
                }
                buttonPanel.add(groupPanel);
                buttonPanel.add(Box.createVerticalStrut(15));
            }
        } else {
            Set<String> allowedFunctions = RoleAccessControl.getFunctions(role);
            for (String function : allowedFunctions) {
                JButton btn = allButtons.get(function);
                if (btn != null) {
                    buttonPanel.add(btn);
                    buttonPanel.add(Box.createVerticalStrut(5));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(buttonPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    @Override
    public void handleEvents() {
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                System.out.println("Logout button clicked...");
                int confirm = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    System.out.println("Logout Confirmed.");
                    SessionManager.clear(); // clear session
                    dispose(); // close HomePage
                    new LoginPage().setVisible(true); // reopen login
                }
            }
        });

        editInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                new AccountFormPage(Mode.EDIT_SELF, userName, HomePage.this);
                dispose();
            }
        });
    }

    @Override
    public void updateState() {
        // Refresh user data if needed
    }
}