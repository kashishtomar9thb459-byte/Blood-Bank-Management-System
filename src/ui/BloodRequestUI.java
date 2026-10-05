package ui;

import exception.BloodBankException;
import model.BloodRequest;
import service.BloodRequestService;
import service.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BloodRequestUI extends JFrame {

    private final BloodRequestService requestService = new BloodRequestService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
    private static final String[] STATUSES = {"All", "Pending", "Approved", "Rejected", "Completed"};

    private JTextField patientField, hospitalField, contactField, unitsField;
    private JComboBox<String> bloodGroupBox, statusFilterBox;
    private JTable table;
    private DefaultTableModel tableModel;

    public BloodRequestUI() {
        setTitle("Blood Bank Management System - Blood Requests");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        loadAllRequests();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UIConstants.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Blood Request Management");
        title.setFont(UIConstants.FONT_TITLE);
        root.add(title, BorderLayout.NORTH);

        root.add(buildFormPanel(), BorderLayout.WEST);
        root.add(buildTablePanel(), BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setPreferredSize(new Dimension(320, 0));
        wrapper.setBackground(UIConstants.CARD_BG);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 6));
        form.setBackground(UIConstants.CARD_BG);

        patientField = new JTextField();
        hospitalField = new JTextField();
        contactField = new JTextField();
        bloodGroupBox = new JComboBox<>(BLOOD_GROUPS);
        unitsField = new JTextField();

        form.add(labeled("Patient Name:", patientField));
        form.add(labeled("Hospital Name:", hospitalField));
        form.add(labeled("Contact Number:", contactField));
        form.add(labeled("Blood Group:", bloodGroupBox));
        form.add(labeled("Units Required:", unitsField));

        wrapper.add(form, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 6, 6));
        buttons.setBackground(UIConstants.CARD_BG);
        buttons.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JButton submitBtn = button("Submit Request", UIConstants.SUCCESS, e -> submitRequest());
        JButton approveBtn = button("Approve Selected", UIConstants.PRIMARY, e -> changeStatus("Approved"));
        JButton completeBtn = button("Mark Completed", UIConstants.PRIMARY_DARK, e -> changeStatus("Completed"));
        JButton rejectBtn = button("Reject Selected", UIConstants.DANGER, e -> changeStatus("Rejected"));
        JButton clearBtn = button("Clear Form", Color.GRAY, e -> clearForm());

        buttons.add(submitBtn);
        buttons.add(approveBtn);
        buttons.add(completeBtn);
        buttons.add(rejectBtn);
        buttons.add(clearBtn);

        wrapper.add(buttons, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(UIConstants.BACKGROUND);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(UIConstants.BACKGROUND);
        statusFilterBox = new JComboBox<>(STATUSES);
        JButton filterBtn = button("Filter", UIConstants.PRIMARY, e -> filterByStatus());
        filterPanel.add(new JLabel("Filter by Status:"));
        filterPanel.add(statusFilterBox);
        filterPanel.add(filterBtn);
        panel.add(filterPanel, BorderLayout.NORTH);

        String[] columns = {"Request ID", "Patient", "Hospital", "Contact", "Blood Group", "Units", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.getTableHeader().setFont(UIConstants.FONT_TABLE_HEADER);
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

    private void submitRequest() {
        try {
            ValidationUtil.requireNonEmpty(patientField.getText(), "Patient name");
            ValidationUtil.requireNonEmpty(hospitalField.getText(), "Hospital name");
            ValidationUtil.validatePhoneNumber(contactField.getText());
            String bloodGroup = (String) bloodGroupBox.getSelectedItem();
            int units = ValidationUtil.parseUnits(unitsField.getText());
            ValidationUtil.validatePositiveUnits(units);

            BloodRequest request = new BloodRequest();
            request.setPatientName(patientField.getText().trim());
            request.setHospitalName(hospitalField.getText().trim());
            request.setContactNumber(contactField.getText().trim());
            request.setBloodGroup(bloodGroup);
            request.setUnitsRequired(units);
            request.setRequestDate(LocalDate.now());

            requestService.addRequest(request);
            JOptionPane.showMessageDialog(this, "Blood request submitted with status: Pending.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadAllRequests();
        } catch (BloodBankException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void changeStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a request from the table first.",
                    "No Request Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int requestId = (int) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Change status of Request #" + requestId + " to '" + newStatus + "'?",
                "Confirm Status Change", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            requestService.updateStatus(requestId, newStatus);
            JOptionPane.showMessageDialog(this, "Request status updated to '" + newStatus + "'."
                    + (newStatus.equals("Approved") || newStatus.equals("Completed")
                        ? "\nBlood stock has been updated accordingly." : ""),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAllRequests();
        } catch (BloodBankException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Operation Not Allowed", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void filterByStatus() {
        String status = (String) statusFilterBox.getSelectedItem();
        if ("All".equals(status)) {
            loadAllRequests();
            return;
        }
        try {
            populateTable(requestService.searchByStatus(status));
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void loadAllRequests() {
        try {
            populateTable(requestService.getAllRequests());
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void populateTable(List<BloodRequest> requests) {
        tableModel.setRowCount(0);
        for (BloodRequest r : requests) {
            tableModel.addRow(new Object[]{
                    r.getRequestId(), r.getPatientName(), r.getHospitalName(), r.getContactNumber(),
                    r.getBloodGroup(), r.getUnitsRequired(), r.getRequestDate().format(DATE_FMT), r.getStatus()
            });
        }
    }

    private void clearForm() {
        patientField.setText("");
        hospitalField.setText("");
        contactField.setText("");
        bloodGroupBox.setSelectedIndex(0);
        unitsField.setText("");
    }

    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
