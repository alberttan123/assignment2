package com.assignment2.gui_albert;

import com.assignment2.helpers.JsonStorageHelper;
import com.assignment2.helpers.SimpleDocumentChangeListener;
import com.assignment2.session.SessionManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AccountFormPage extends GUI {

    private JFrame previousWindow;
    private JLabel profileLabel;
    private JTextField emailField, firstNameField, lastNameField;
    private JComboBox<String> dayBox, monthBox, yearBox;
    private JRadioButton salesManagerBtn, inventoryManagerBtn, financeManagerBtn, purchaseManagerBtn, adminBtn;
    private ButtonGroup roleGroup;
    private JButton registerButton, uploadPfpButton;

    public enum Mode {
        CREATE,
        EDIT_SELF,
        EDIT_OTHER
    }

    private Mode mode;
    private String editingEmail; // target email for edit modes
    private static final String USER_PATH = "data/users.txt";

    private String originalEmail = "";
    private String originalFirstName = "";
    private String originalLastName = "";
    private String originalDay = "", originalMonth = "", originalYear = "";
    private String originalRole = "";
    private boolean formChanged = false;

    public AccountFormPage(Mode mode, String emailToEdit, JFrame previousWindow) {
        this.mode = mode;
        this.editingEmail = emailToEdit;
        this.previousWindow = previousWindow;
        this.windowWidth = 800;
        this.windowHeight = 300;
        initWindow(getWindowTitle());

        render();
        handleEvents();
        
        // Handle closing via X button
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (previousWindow != null) {
                    previousWindow.setVisible(true);
                }
                dispose(); // Close this window
            }
        });

        setVisible(true);
    }

    public AccountFormPage(Mode mode, JFrame previousWindow) {
        this.mode = mode;
        this.previousWindow = previousWindow;
        this.windowWidth = 800;
        this.windowHeight = 300;
        initWindow(getWindowTitle());

        render();
        handleEvents();
        
        // Handle closing via X button
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (previousWindow != null) {
                    previousWindow.setVisible(true);
                }
                dispose(); // Close this window
            }
        });

        setVisible(true);
    }

    private String getWindowTitle() {
        switch (mode) {
            case EDIT_SELF:
                return "Edit Personal Info";
            case EDIT_OTHER:
                return "Edit User";
            case CREATE:
            default:
                return "Create New Account";
        }
    }

    @Override
    public void render() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        // Left: Profile Picture
        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));
        profileLabel = new JLabel("[PFP]");
        profileLabel.setPreferredSize(new Dimension(100, 100));
        profileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadPfpButton = new JButton("Upload Profile Picture");
        uploadPfpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftTopPanel.add(profileLabel);
        leftTopPanel.add(Box.createVerticalStrut(10));
        leftTopPanel.add(uploadPfpButton);

        // Right: Info Fields
        JPanel rightTopPanel = new JPanel();
        rightTopPanel.setLayout(new BoxLayout(rightTopPanel, BoxLayout.Y_AXIS));
        
        JPanel emailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JPanel bruh = new JPanel(); // Keep this, unfortunately required to align things properly
        bruh.setLayout(new BoxLayout(bruh, BoxLayout.Y_AXIS));
        JLabel emailLabel = new JLabel("Email");
        emailField = new JTextField(30);
        emailField.setBackground(Color.WHITE);
        emailField.setOpaque(true);
        emailField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        bruh.add(emailLabel);
        bruh.add(emailField);
        emailPanel.add(bruh);

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JPanel firstNamePanel = new JPanel();
        firstNamePanel.setLayout(new BoxLayout(firstNamePanel, BoxLayout.Y_AXIS));
        JLabel firstNameLabel = new JLabel("First Name");
        firstNameField = new JTextField(10);
        firstNameField.setBackground(Color.WHITE);
        firstNameField.setOpaque(true);
        firstNameField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        firstNamePanel.add(firstNameLabel);
        firstNamePanel.add(firstNameField);

        JPanel lastNamePanel = new JPanel();
        lastNamePanel.setLayout(new BoxLayout(lastNamePanel, BoxLayout.Y_AXIS));
        JLabel lastNameLabel = new JLabel("Last Name");
        lastNameField = new JTextField(10);
        lastNameField.setBackground(Color.WHITE);
        lastNameField.setOpaque(true);
        lastNameField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lastNamePanel.add(lastNameLabel);
        lastNamePanel.add(lastNameField);

        namePanel.add(firstNamePanel);
        namePanel.add(lastNamePanel);

        JPanel dobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JPanel dayPanel = new JPanel();
        dayPanel.setLayout(new BoxLayout(dayPanel, BoxLayout.Y_AXIS));
        JLabel dayLabel = new JLabel("Day");
        dayBox = new JComboBox<>(generateNumberOptions(1, 31));
        dayBox.setBackground(Color.WHITE);
        dayPanel.add(dayLabel);
        dayPanel.add(dayBox);

        JPanel monthPanel = new JPanel();
        monthPanel.setLayout(new BoxLayout(monthPanel, BoxLayout.Y_AXIS));
        JLabel monthLabel = new JLabel("Month");
        monthBox = new JComboBox<>(generateNumberOptions(1, 12));
        monthBox.setBackground(Color.WHITE);
        monthPanel.add(monthLabel);
        monthPanel.add(monthBox);

        JPanel yearPanel = new JPanel();
        yearPanel.setLayout(new BoxLayout(yearPanel, BoxLayout.Y_AXIS));
        JLabel yearLabel = new JLabel("Year");
        yearBox = new JComboBox<>(generateNumberOptions(1960, 2024));
        yearBox.setBackground(Color.WHITE);
        yearPanel.add(yearLabel);
        yearPanel.add(yearBox);

        dobPanel.add(dayPanel);
        dobPanel.add(monthPanel);
        dobPanel.add(yearPanel);

        rightTopPanel.add(Box.createVerticalStrut(10));
        rightTopPanel.add(emailPanel);
        rightTopPanel.add(Box.createVerticalStrut(10));
        rightTopPanel.add(namePanel);
        rightTopPanel.add(Box.createVerticalStrut(10));
        rightTopPanel.add(dobPanel);

        rightTopPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));

        topPanel.add(leftTopPanel);
        topPanel.add(rightTopPanel);

        topPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Middle Panel: Role Selection
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        salesManagerBtn = new JRadioButton("Sales Manager");
        inventoryManagerBtn = new JRadioButton("Inventory Manager");
        financeManagerBtn = new JRadioButton("Finance Manager");
        purchaseManagerBtn = new JRadioButton("Purchase Manager");
        adminBtn = new JRadioButton("Administrator");

        roleGroup = new ButtonGroup();
        roleGroup.add(salesManagerBtn);
        roleGroup.add(inventoryManagerBtn);
        roleGroup.add(financeManagerBtn);
        roleGroup.add(purchaseManagerBtn);
        roleGroup.add(adminBtn);

        rolePanel.add(salesManagerBtn);
        rolePanel.add(inventoryManagerBtn);
        rolePanel.add(financeManagerBtn);
        rolePanel.add(purchaseManagerBtn);
        rolePanel.add(adminBtn);

        // Bottom Panel: Button
        JPanel bottomPanel = new JPanel();
        registerButton = new JButton(mode == Mode.CREATE ? "Register" : "Save");
        bottomPanel.add(registerButton);

        boolean isEditableEmail = mode == Mode.CREATE;
        boolean isEditableRoles = mode != Mode.EDIT_SELF;

        emailField.setEditable(isEditableEmail);
        emailField.setFocusable(isEditableEmail);
        salesManagerBtn.setEnabled(isEditableRoles);
        inventoryManagerBtn.setEnabled(isEditableRoles);
        financeManagerBtn.setEnabled(isEditableRoles);
        purchaseManagerBtn.setEnabled(isEditableRoles);
        adminBtn.setEnabled(isEditableRoles);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(rolePanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        if (mode != Mode.CREATE && editingEmail != null) {
            loadUserData(editingEmail);
        }

        addChangeListeners();
    }

    private void loadUserData(String email) {
        try {
            JsonObject root = JsonStorageHelper.loadAsJsonObject(USER_PATH);
            JsonArray users = root.getAsJsonArray("users");
            for (int i = 0; i < users.size(); i++) {
                JsonObject user = users.get(i).getAsJsonObject();
                if (user.get("email").getAsString().equalsIgnoreCase(email)) {
                    emailField.setText(user.get("email").getAsString());
                    originalEmail = user.get("email").getAsString();

                    firstNameField.setText(user.get("name").getAsJsonObject().get("fname").getAsString());
                    originalFirstName = user.get("name").getAsJsonObject().get("fname").getAsString();
                    lastNameField.setText(user.get("name").getAsJsonObject().get("lname").getAsString());
                    originalLastName = user.get("name").getAsJsonObject().get("lname").getAsString();

                    JsonObject dob = user.get("dob").getAsJsonObject();
                    dayBox.setSelectedItem(dob.get("day").getAsString());
                    originalDay = dob.get("day").getAsString();
                    monthBox.setSelectedItem(dob.get("month").getAsString());
                    originalMonth = dob.get("month").getAsString();
                    yearBox.setSelectedItem(dob.get("year").getAsString());
                    originalYear = dob.get("year").getAsString();

                    if(SessionManager.checkPfpExists()){
                        profileLabel.setText("");
                        profileLabel.setIcon(new ImageIcon(SessionManager.getPfp()));
                    }

                    String role = user.get("role").getAsString();
                    originalRole = role;
                    switch (role) {
                        case "sales_manager": salesManagerBtn.setSelected(true); break;
                        case "inventory_manager": inventoryManagerBtn.setSelected(true); break;
                        case "finance_manager": financeManagerBtn.setSelected(true); break;
                        case "purchase_manager": purchaseManagerBtn.setSelected(true); break;
                        case "admin": adminBtn.setSelected(true); break;
                    }
                    break;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading user data.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvents() {
        registerButton.addActionListener(e -> {
            String action;
            switch (mode) {
                case CREATE: action = "Registering"; break;
                case EDIT_SELF: action = "Saving personal info for"; break;
                case EDIT_OTHER: action = "Updating user info for"; break;
                default: action = ""; break;
            }
            
            String emailInput = emailField.getText().trim();
            String firstNameInput = firstNameField.getText().trim();
            String lastNameInput = lastNameField.getText().trim();

            if (emailInput.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!emailInput.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                JOptionPane.showMessageDialog(this, "Email format is invalid.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String> existingValues = JsonStorageHelper.getUserList();
            for (int i = 0; i < existingValues.size(); i++) {
                existingValues.set(i, existingValues.get(i).toLowerCase());
            }

            if (existingValues.contains(emailInput) && mode == Mode.CREATE) {
                JOptionPane.showMessageDialog(this, "This email is already taken.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (firstNameInput.isEmpty()) {
                JOptionPane.showMessageDialog(this, "First name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (lastNameInput.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Last name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedRole = null;
            if (salesManagerBtn.isSelected()) selectedRole = "sales_manager";
            else if (inventoryManagerBtn.isSelected()) selectedRole = "inventory_manager";
            else if (financeManagerBtn.isSelected()) selectedRole = "finance_manager";
            else if (purchaseManagerBtn.isSelected()) selectedRole = "purchase_manager";
            else if (adminBtn.isSelected()) selectedRole = "admin";

            if (selectedRole == null) {
                JOptionPane.showMessageDialog(this, "Please select a role.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JsonObject root;
            try {
                root = JsonStorageHelper.loadAsJsonObject(USER_PATH);
                JsonArray users = root.getAsJsonArray("users");
                String email = emailField.getText().toLowerCase();

                JsonObject name = new JsonObject();
                name.addProperty("fname", firstNameField.getText());
                name.addProperty("lname", lastNameField.getText());

                JsonObject dob = new JsonObject();
                dob.addProperty("day", dayBox.getSelectedItem().toString());
                dob.addProperty("month", monthBox.getSelectedItem().toString());
                dob.addProperty("year", yearBox.getSelectedItem().toString());

                JsonObject updatedUser = new JsonObject();
                updatedUser.addProperty("email", email);
                updatedUser.add("name", name);
                updatedUser.add("dob", dob);
                updatedUser.addProperty("role", selectedRole != null ? selectedRole : "sales_manager");

                if (mode == Mode.CREATE) {
                    String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    updatedUser.addProperty("createdAt", now);
                    updatedUser.addProperty("profilePicturePath", "");
                    updatedUser.addProperty("userId", JsonStorageHelper.getNextUserId());
                }

                boolean userExists = false;

                for (int i = 0; i < users.size(); i++) {
                    JsonObject existingUser = users.get(i).getAsJsonObject();
                    if ((mode == Mode.EDIT_OTHER && existingUser.get("email").getAsString().equalsIgnoreCase(editingEmail)) ||
                        (mode != Mode.EDIT_OTHER && existingUser.get("email").getAsString().equalsIgnoreCase(email))) {
                        userExists = true;

                        if (mode == Mode.EDIT_SELF || mode == Mode.EDIT_OTHER) {
                            // Preserve password and createdAt and pfp
                            if (existingUser.has("password"))
                                updatedUser.add("password", existingUser.get("password"));
                                updatedUser.add("saltStr", existingUser.get("saltStr"));
                            if (existingUser.has("createdAt"))
                                updatedUser.add("createdAt", existingUser.get("createdAt"));
                            if (existingUser.has("userId"))
                                updatedUser.add("userId", existingUser.get("userId"));
                            if (existingUser.has("profilePicturePath") && !updatedUser.has("profilePicturePath"))
                                updatedUser.add("profilePicturePath", existingUser.get("profilePicturePath"));

                            users.set(i, updatedUser);
                        } else if (mode == Mode.CREATE) {
                            JOptionPane.showMessageDialog(this, "A user with this email already exists.");
                            return;
                        }

                        break;
                    }
                }

                if (mode == Mode.CREATE && !userExists) {
                    users.add(updatedUser);
                }

                JsonStorageHelper.saveToJson(USER_PATH, root);
            } catch (IOException e1) {
                e1.printStackTrace();
                System.out.println("File not found");
            }

            // Reload user data in Session Handler
            SessionManager.reloadCurrentUser();

            // Close page after saving
            if (previousWindow instanceof TablePage) {
                try{
                    JsonObject bruh = JsonStorageHelper.loadAsJsonObject(USER_PATH);
                    JsonArray users = bruh.getAsJsonArray("users");
                    TablePage tablePage = (TablePage) previousWindow;
                    tablePage.refreshTableData(users);
                }catch (IOException e1){
                    e1.printStackTrace();
                    System.out.println("File not found.");
                }
            }

            previousWindow.setVisible(true);

            dispose();
        });

        uploadPfpButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Profile Picture");

            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif"
            ));

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                try {
                    // Resize image
                    ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
                    Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    profileLabel.setText("");
                    profileLabel.setIcon(new ImageIcon(scaledImage));

                    // Obfuscate email
                    String email = emailField.getText();
                    String obfuscated = obfuscateEmail(email);
                    String outputPath = "data/images/" + obfuscated + ".jpg";

                    // Ensure directory exists
                    File dir = new File("data/images");
                    if (!dir.exists()) dir.mkdirs();

                    // Convert Image to BufferedImage and write as JPEG
                    BufferedImage bufferedImage = new BufferedImage(
                        scaledImage.getWidth(null),
                        scaledImage.getHeight(null),
                        BufferedImage.TYPE_INT_RGB
                    );
                    Graphics2D g2d = bufferedImage.createGraphics();
                    g2d.drawImage(scaledImage, 0, 0, null);
                    g2d.dispose();

                    ImageIO.write(bufferedImage, "jpg", new File(outputPath));

                    // Update users.txt with profilePicturePath
                    JsonObject root = JsonStorageHelper.loadAsJsonObject(USER_PATH);
                    JsonArray users = root.getAsJsonArray("users");

                    for (int i = 0; i < users.size(); i++) {
                        JsonObject user = users.get(i).getAsJsonObject();
                        if (user.get("email").getAsString().equalsIgnoreCase(email)) {
                            user.addProperty("profilePicturePath", outputPath);
                            break;
                        }
                    }

                    JsonStorageHelper.saveToJson(USER_PATH, root);

                    formChanged = true;
                    registerButton.setEnabled(true);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to load and save image.", "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void updateState() {
        // Optional: implement to update UI state dynamically
    }

    private String[] generateNumberOptions(int start, int end) {
        String[] options = new String[end - start + 1];
        for (int i = 0; i < options.length; i++) {
            options[i] = String.valueOf(start + i);
        }
        return options;
    }

    private void checkForChanges() {
        boolean hasChanged = !emailField.getText().equals(originalEmail) ||
                !firstNameField.getText().equals(originalFirstName) ||
                !lastNameField.getText().equals(originalLastName) ||
                !dayBox.getSelectedItem().toString().equals(originalDay) ||
                !monthBox.getSelectedItem().toString().equals(originalMonth) ||
                !yearBox.getSelectedItem().toString().equals(originalYear) ||
                !getSelectedRole().equals(originalRole);

        if (hasChanged != formChanged) {
            formChanged = hasChanged;
            registerButton.setEnabled(formChanged);
        }
    }

    private String getSelectedRole() {
        if (salesManagerBtn.isSelected()) return "sales_manager";
        if (inventoryManagerBtn.isSelected()) return "inventory_manager";
        if (financeManagerBtn.isSelected()) return "finance_manager";
        if (purchaseManagerBtn.isSelected()) return "purchase_manager";
        if (adminBtn.isSelected()) return "admin";
        return "";
    }

    private void addChangeListeners(){
        // Disable save by default
        registerButton.setEnabled(false);

        // Add listeners to detect changes
        emailField.getDocument().addDocumentListener(new SimpleDocumentChangeListener(this::checkForChanges));
        firstNameField.getDocument().addDocumentListener(new SimpleDocumentChangeListener(this::checkForChanges));
        lastNameField.getDocument().addDocumentListener(new SimpleDocumentChangeListener(this::checkForChanges));

        dayBox.addActionListener(e -> checkForChanges());
        monthBox.addActionListener(e -> checkForChanges());
        yearBox.addActionListener(e -> checkForChanges());

        salesManagerBtn.addActionListener(e -> checkForChanges());
        inventoryManagerBtn.addActionListener(e -> checkForChanges());
        financeManagerBtn.addActionListener(e -> checkForChanges());
        purchaseManagerBtn.addActionListener(e -> checkForChanges());
        adminBtn.addActionListener(e -> checkForChanges());
    }

    private String obfuscateEmail(String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.toLowerCase().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 6; i++) { // 6 bytes = 12 hex chars = short and safe
                hex.append(String.format("%02x", hash[i]));
            }
            return hex.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "default";
        }
    }
}
