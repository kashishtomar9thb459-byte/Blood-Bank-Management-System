package ui;

import exception.BloodBankException;
import model.Donor;
import service.DonorService;
import service.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class DonorManagementUI extends JFrame {

    private final DonorService donorService = new DonorService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    private JTextField idField, nameField, ageField, phoneField, emailField, addressField, lastDonationField;
    private JComboBox<String> genderBox, bloodGroupBox;
    private JTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;

    public DonorManagementUI() {
        setTitle("Blood Bank Management System - Donor Management");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        loadAllDonors();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UIConstants.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Donor Management");
        title.setFont(UIConstants.FONT_TITLE);
        root.add(title, BorderLayout.NORTH);

        root.add(buildFormPanel(), BorderLayout.WEST);
        root.add(buildTablePanel(), BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildFormPanel() {
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setPreferredSize(new Dimension(320, 0));
        formWrapper.setBackground(UIConstants.CARD_BG);
        formWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 6));
        form.setBackground(UIConstants.CARD_BG);

        idField = new JTextField();
        idField.setEditable(false);
        nameField = new JTextField();
        ageField = new JTextField();
        genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        bloodGroupBox = new JComboBox<>(BLOOD_GROUPS);
        phoneField = new JTextField();
        emailField = new JTextField();
        addressField = new JTextField();
        lastDonationField = new JTextField();
        lastDonationField.setToolTipText("Format: yyyy-MM-dd, leave blank if never donated");

        form.add(labeled("Donor ID (auto):", idField));
        form.add(labeled("Full Name:", nameField));
        form.add(labeled("Age:", ageField));
        form.add(labeled("Gender:", genderBox));
        form.add(labeled("Blood Group:", bloodGroupBox));
        form.add(labeled("Phone Number:", phoneField));
        form.add(labeled("Email:", emailField));
        form.add(labeled("Address:", addressField));
        form.add(labeled("Last Donation Date:", lastDonationField));

        formWrapper.add(form, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(0, 2, 6, 6));
        buttons.setBackground(UIConstants.CARD_BG);
        buttons.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JButton addBtn = button("Add Donor", UIConstants.SUCCESS, e -> addDonor());
        JButton updateBtn = button("Update Donor", UIConstants.WARNING, e -> updateDonor());
        JButton deleteBtn = button("Delete Donor", UIConstants.DANGER, e -> deleteDonor());
        JButton clearBtn = button("Clear Form", Color.GRAY, e -> clearForm());

        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(clearBtn);

        formWrapper.add(buttons, BorderLayout.SOUTH);
        return formWrapper;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(UIConstants.BACKGROUND);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(UIConstants.BACKGROUND);
        searchField = new JTextField(18);
        JButton searchBtn = button("Search by Name", UIConstants.PRIMARY, e -> searchByName());
        JButton viewAllBtn = button("View All", UIConstants.PRIMARY_DARK, e -> loadAllDonors());
        searchPanel.add(new JLabel("Search Donor:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(viewAllBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Age", "Gender", "Blood Group", "Phone", "Email", "Address", "Last Donation"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.getTableHeader().setFont(UIConstants.FONT_TABLE_HEADER);
        table.getSelectionModel().addListSelectionListener(e -> populateFormFromSelection());

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.CARD_BG);
        JLabel l = new JLabel(label);
        l.setFont(UIConstants.FONT_LABEL);
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JButton button(String text, Color bg, java.awt.event.ActionListener listener) {
        JButton b = new JButton(text);
        b.setFont(UIConstants.FONT_BUTTON);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.addActionListener(listener);
        return b;
    }

    // ---------------- CRUD actions ----------------

    private void addDonor() {
        try {
            Donor donor = readFormAsDonor(false);
            int newId = donorService.addDonor(donor);
            JOptionPane.showMessageDialog(this, "Donor added successfully with ID " + newId + ".",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadAllDonors();
        } catch (BloodBankException ex) {
            showValidationError(ex.getMessage());
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void updateDonor() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a donor from the table first.",
                    "No Donor Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Donor donor = readFormAsDonor(true);
            donorService.updateDonor(donor);
            JOptionPane.showMessageDialog(this, "Donor updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadAllDonors();
        } catch (BloodBankException ex) {
            showValidationError(ex.getMessage());
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void deleteDonor() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a donor from the table first.",
                    "No Donor Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this donor permanently?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            int id = Integer.parseInt(idField.getText().trim());
            donorService.deleteDonor(id);
            JOptionPane.showMessageDialog(this, "Donor deleted successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadAllDonors();
        } catch (BloodBankException ex) {
            showValidationError(ex.getMessage());
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void searchByName() {
        String name = searchField.getText().trim();
        if (name.isEmpty()) {
            loadAllDonors();
            return;
        }
        try {
            populateTable(donorService.searchByName(name));
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void loadAllDonors() {
        try {
            populateTable(donorService.getAllDonors());
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // ---------------- Helpers ----------------

    private Donor readFormAsDonor(boolean isUpdate) throws BloodBankException {
        ValidationUtil.requireNonEmpty(nameField.getText(), "Full name");
        int age = ValidationUtil.parseAge(ageField.getText());
        ValidationUtil.validateAge(age);
        String gender = (String) genderBox.getSelectedItem();
        String bloodGroup = (String) bloodGroupBox.getSelectedItem();
        ValidationUtil.validatePhoneNumber(phoneField.getText());

        LocalDate lastDonation = null;
        String lastDonationText = lastDonationField.getText().trim();
        if (!lastDonationText.isEmpty()) {
            try {
                lastDonation = LocalDate.parse(lastDonationText, DATE_FMT);
            } catch (DateTimeParseException e) {
                throw new BloodBankException("Last donation date must be in yyyy-MM-dd format.");
            }
            ValidationUtil.validateDateNotFuture(lastDonation);
        }

        Donor donor = new Donor(nameField.getText().trim(), age, gender, bloodGroup,
                phoneField.getText().trim(), emailField.getText().trim(),
                addressField.getText().trim(), lastDonation);

        if (isUpdate) {
            donor.setDonorId(Integer.parseInt(idField.getText().trim()));
        }
        return donor;
    }

    private void populateTable(List<Donor> donors) {
        tableModel.setRowCount(0);
        for (Donor d : donors) {
            tableModel.addRow(new Object[]{
                    d.getDonorId(), d.getFullName(), d.getAge(), d.getGender(), d.getBloodGroup(),
                    d.getPhoneNumber(), d.getEmail(), d.getAddress(),
                    d.getLastDonationDate() != null ? d.getLastDonationDate().format(DATE_FMT) : ""
            });
        }
    }

    private void populateFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        idField.setText(tableModel.getValueAt(row, 0).toString());
        nameField.setText(tableModel.getValueAt(row, 1).toString());
        ageField.setText(tableModel.getValueAt(row, 2).toString());
        genderBox.setSelectedItem(tableModel.getValueAt(row, 3).toString());
        bloodGroupBox.setSelectedItem(tableModel.getValueAt(row, 4).toString());
        phoneField.setText(tableModel.getValueAt(row, 5).toString());
        emailField.setText(tableModel.getValueAt(row, 6) == null ? "" : tableModel.getValueAt(row, 6).toString());
        addressField.setText(tableModel.getValueAt(row, 7) == null ? "" : tableModel.getValueAt(row, 7).toString());
        lastDonationField.setText(tableModel.getValueAt(row, 8) == null ? "" : tableModel.getValueAt(row, 8).toString());
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        ageField.setText("");
        genderBox.setSelectedIndex(0);
        bloodGroupBox.setSelectedIndex(0);
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        lastDonationField.setText("");
        table.clearSelection();
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
