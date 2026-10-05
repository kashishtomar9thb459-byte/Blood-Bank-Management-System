package ui;

import model.BloodRequest;
import model.BloodStock;
import model.Donation;
import model.Donor;
import service.BloodRequestService;
import service.BloodStockService;
import service.DonationService;
import service.DonorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsUI extends JFrame {

    private final DonorService donorService = new DonorService();
    private final BloodStockService stockService = new BloodStockService();
    private final DonationService donationService = new DonationService();
    private final BloodRequestService requestService = new BloodRequestService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReportsUI() {
        setTitle("Blood Bank Management System - Reports");
        setSize(950, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UIConstants.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Reports");
        title.setFont(UIConstants.FONT_TITLE);
        root.add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("All Donors", buildDonorReportTab());
        tabs.addTab("Blood Stock", buildStockReportTab());
        tabs.addTab("Donation History", buildDonationReportTab());
        tabs.addTab("Blood Requests", buildRequestReportTab());

        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ---------- Donor report ----------
    private JPanel buildDonorReportTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "Name", "Age", "Gender", "Blood Group", "Phone", "Email", "Last Donation"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> groupFilter = new JComboBox<>(new String[]{"All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        JButton loadBtn = new JButton("Load Report");
        loadBtn.addActionListener(e -> {
            try {
                String group = (String) groupFilter.getSelectedItem();
                List<Donor> donors = "All".equals(group) ? donorService.getAllDonors() : donorService.searchByBloodGroup(group);
                model.setRowCount(0);
                for (Donor d : donors) {
                    model.addRow(new Object[]{d.getDonorId(), d.getFullName(), d.getAge(), d.getGender(),
                            d.getBloodGroup(), d.getPhoneNumber(), d.getEmail(),
                            d.getLastDonationDate() != null ? d.getLastDonationDate().format(DATE_FMT) : "Never"});
                }
            } catch (SQLException ex) {
                showDbError(ex);
            }
        });
        filterRow.add(new JLabel("Filter by Blood Group:"));
        filterRow.add(groupFilter);
        filterRow.add(loadBtn);

        panel.add(filterRow, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        loadBtn.doClick();
        return panel;
    }

    // ---------- Blood stock report ----------
    private JPanel buildStockReportTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Blood Group", "Available Units", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadBtn = new JButton("Load Report");
        loadBtn.addActionListener(e -> {
            try {
                List<BloodStock> stockList = stockService.getAllStock();
                model.setRowCount(0);
                for (BloodStock s : stockList) {
                    model.addRow(new Object[]{s.getBloodGroup(), s.getUnitsAvailable(), s.getStatus()});
                }
            } catch (SQLException ex) {
                showDbError(ex);
            }
        });
        topRow.add(loadBtn);

        panel.add(topRow, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        loadBtn.doClick();
        return panel;
    }

    // ---------- Donation history report ----------
    private JPanel buildDonationReportTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Donation ID", "Donor ID", "Donor Name", "Blood Group", "Date", "Units"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField nameFilter = new JTextField(15);
        JButton loadBtn = new JButton("Load Report");
        loadBtn.addActionListener(e -> {
            try {
                String name = nameFilter.getText().trim();
                List<Donation> donations = name.isEmpty() ? donationService.getAllDonations() : donationService.searchByDonorName(name);
                model.setRowCount(0);
                for (Donation d : donations) {
                    model.addRow(new Object[]{d.getDonationId(), d.getDonorId(), d.getDonorName(),
                            d.getBloodGroup(), d.getDonationDate().format(DATE_FMT), d.getUnitsDonated()});
                }
            } catch (SQLException ex) {
                showDbError(ex);
            }
        });
        filterRow.add(new JLabel("Filter by Donor Name:"));
        filterRow.add(nameFilter);
        filterRow.add(loadBtn);

        panel.add(filterRow, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        loadBtn.doClick();
        return panel;
    }

    // ---------- Blood request report ----------
    private JPanel buildRequestReportTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Request ID", "Patient", "Hospital", "Contact", "Blood Group", "Units", "Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Rejected", "Completed"});
        JButton loadBtn = new JButton("Load Report");
        loadBtn.addActionListener(e -> {
            try {
                String status = (String) statusFilter.getSelectedItem();
                List<BloodRequest> requests = "All".equals(status) ? requestService.getAllRequests() : requestService.searchByStatus(status);
                model.setRowCount(0);
                for (BloodRequest r : requests) {
                    model.addRow(new Object[]{r.getRequestId(), r.getPatientName(), r.getHospitalName(),
                            r.getContactNumber(), r.getBloodGroup(), r.getUnitsRequired(),
                            r.getRequestDate().format(DATE_FMT), r.getStatus()});
                }
            } catch (SQLException ex) {
                showDbError(ex);
            }
        });
        filterRow.add(new JLabel("Filter by Status:"));
        filterRow.add(statusFilter);
        filterRow.add(loadBtn);

        panel.add(filterRow, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        loadBtn.doClick();
        return panel;
    }

    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
