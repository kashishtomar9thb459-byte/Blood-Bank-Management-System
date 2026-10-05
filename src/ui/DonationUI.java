package ui;

import exception.BloodBankException;
import model.Donation;
import model.Donor;
import service.DonationService;
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

public class DonationUI extends JFrame {

    private final DonationService donationService = new DonationService();
    private final DonorService donorService = new DonorService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private JComboBox<DonorItem> donorBox;
    private JTextField bloodGroupField, dateField, unitsField, searchField;
    private JTable table;
    private DefaultTableModel tableModel;

    public DonationUI() {
        setTitle("Blood Bank Management System - Donation Management");
        setSize(950, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        loadDonors();
        loadAllDonations();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UIConstants.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Donation Management");
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

        donorBox = new JComboBox<>();
        donorBox.addActionListener(e -> autoFillBloodGroup());

        bloodGroupField = new JTextField();
        bloodGroupField.setEditable(false);

        dateField = new JTextField(LocalDate.now().format(DATE_FMT));
        unitsField = new JTextField();

        form.add(labeled("Donor:", donorBox));
        form.add(labeled("Blood Group (auto):", bloodGroupField));
        form.add(labeled("Donation Date (yyyy-MM-dd):", dateField));
        form.add(labeled("Units Donated:", unitsField));

        wrapper.add(form, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 6, 6));
        buttons.setBackground(UIConstants.CARD_BG);
        buttons.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JButton recordBtn = button("Record Donation", UIConstants.SUCCESS, e -> recordDonation());
        JButton refreshDonorsBtn = button("Refresh Donor List", UIConstants.PRIMARY_DARK, e -> loadDonors());
        JButton clearBtn = button("Clear Form", Color.GRAY, e -> clearForm());

        buttons.add(recordBtn);
        buttons.add(refreshDonorsBtn);
        buttons.add(clearBtn);

        wrapper.add(buttons, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(UIConstants.BACKGROUND);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(UIConstants.BACKGROUND);
        searchField = new JTextField(18);
        JButton searchBtn = button("Search by Donor Name", UIConstants.PRIMARY, e -> searchDonations());
        JButton viewAllBtn = button("View All", UIConstants.PRIMARY_DARK, e -> loadAllDonations());
        searchPanel.add(new JLabel("Search Donation History:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(viewAllBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"Donation ID", "Donor ID", "Donor Name", "Blood Group", "Donation Date", "Units"};
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

    private void loadDonors() {
        try {
            List<Donor> donors = donorService.getAllDonors();
            donorBox.removeAllItems();
            for (Donor d : donors) {
                donorBox.addItem(new DonorItem(d));
            }
            autoFillBloodGroup();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void autoFillBloodGroup() {
        DonorItem item = (DonorItem) donorBox.getSelectedItem();
        bloodGroupField.setText(item != null ? item.donor.getBloodGroup() : "");
    }

    private void recordDonation() {
        DonorItem item = (DonorItem) donorBox.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "No donors available. Add a donor first.",
                    "No Donor", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LocalDate date;
            try {
                date = LocalDate.parse(dateField.getText().trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                throw new BloodBankException("Donation date must be in yyyy-MM-dd format.");
            }
            int units = ValidationUtil.parseUnits(unitsField.getText());

            Donation donation = new Donation();
            donation.setDonorId(item.donor.getDonorId());
            donation.setBloodGroup(item.donor.getBloodGroup());
            donation.setDonationDate(date);
            donation.setUnitsDonated(units);

            donationService.recordDonation(donation);
            JOptionPane.showMessageDialog(this,
                    "Donation recorded successfully. Blood stock updated automatically.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadAllDonations();
        } catch (BloodBankException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void searchDonations() {
        String name = searchField.getText().trim();
        if (name.isEmpty()) {
            loadAllDonations();
            return;
        }
        try {
            populateTable(donationService.searchByDonorName(name));
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void loadAllDonations() {
        try {
            populateTable(donationService.getAllDonations());
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void populateTable(List<Donation> donations) {
        tableModel.setRowCount(0);
        for (Donation d : donations) {
            tableModel.addRow(new Object[]{
                    d.getDonationId(), d.getDonorId(), d.getDonorName(), d.getBloodGroup(),
                    d.getDonationDate().format(DATE_FMT), d.getUnitsDonated()
            });
        }
    }

    private void clearForm() {
        dateField.setText(LocalDate.now().format(DATE_FMT));
        unitsField.setText("");
    }

    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Wrapper so the JComboBox shows "ID - Name" but keeps the underlying Donor object. */
    private static class DonorItem {
        final Donor donor;
        DonorItem(Donor donor) { this.donor = donor; }
        @Override
        public String toString() {
            return donor.getDonorId() + " - " + donor.getFullName() + " (" + donor.getBloodGroup() + ")";
        }
    }
}
