package ui;

import model.BloodStock;
import model.Donor;
import service.BloodStockService;
import service.DonorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SearchBloodUI extends JFrame {

    private final BloodStockService stockService = new BloodStockService();
    private final DonorService donorService = new DonorService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    private JComboBox<String> bloodGroupBox;
    private JLabel resultGroup, resultUnits, resultStatus;
    private JTable donorTable;
    private DefaultTableModel donorTableModel;

    public SearchBloodUI() {
        setTitle("Blood Bank Management System - Search Blood");
        setSize(750, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UIConstants.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Search Blood Availability");
        title.setFont(UIConstants.FONT_TITLE);
        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(UIConstants.BACKGROUND);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchRow.setBackground(UIConstants.BACKGROUND);
        bloodGroupBox = new JComboBox<>(BLOOD_GROUPS);
        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(UIConstants.PRIMARY);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.addActionListener(e -> doSearch());
        searchRow.add(new JLabel("Select Blood Group:"));
        searchRow.add(bloodGroupBox);
        searchRow.add(searchBtn);
        center.add(searchRow);

        JPanel resultPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        resultPanel.setBackground(UIConstants.BACKGROUND);
        resultPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        resultPanel.setMaximumSize(new Dimension(700, 110));

        resultGroup = new JLabel("-");
        resultUnits = new JLabel("-");
        resultStatus = new JLabel("-");

        resultPanel.add(resultCard("Blood Group", resultGroup));
        resultPanel.add(resultCard("Available Units", resultUnits));
        resultPanel.add(resultCard("Availability Status", resultStatus));
        center.add(resultPanel);

        JLabel donorLabel = new JLabel("Registered Donors with this Blood Group:");
        donorLabel.setFont(UIConstants.FONT_HEADING);
        donorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(donorLabel);

        String[] columns = {"Donor ID", "Name", "Phone", "Last Donation"};
        donorTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        donorTable = new JTable(donorTableModel);
        donorTable.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(donorTable);
        scrollPane.setPreferredSize(new Dimension(700, 250));
        center.add(scrollPane);

        root.add(center, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel resultCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.PRIMARY, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel textLabel = new JLabel(label, SwingConstants.CENTER);
        textLabel.setFont(UIConstants.FONT_LABEL);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(textLabel, BorderLayout.SOUTH);
        return card;
    }

    private void doSearch() {
        String group = (String) bloodGroupBox.getSelectedItem();
        try {
            BloodStock stock = stockService.getStockByGroup(group);
            resultGroup.setText(group);
            resultUnits.setText(String.valueOf(stock != null ? stock.getUnitsAvailable() : 0));
            resultStatus.setText(stock != null ? stock.getStatus() : "Not Available");

            List<Donor> donors = donorService.searchByBloodGroup(group);
            donorTableModel.setRowCount(0);
            for (Donor d : donors) {
                donorTableModel.addRow(new Object[]{
                        d.getDonorId(), d.getFullName(), d.getPhoneNumber(),
                        d.getLastDonationDate() != null ? d.getLastDonationDate().format(DATE_FMT) : "Never"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
