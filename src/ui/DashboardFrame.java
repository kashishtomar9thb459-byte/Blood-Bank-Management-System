package ui;

import model.Admin;
import service.DashboardService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class DashboardFrame extends JFrame {

    private final Admin admin;
    private final DashboardService dashboardService = new DashboardService();

    private JLabel donorsValue, unitsValue, requestsValue, donationsValue;

    public DashboardFrame(Admin admin) {
        this.admin = admin;
        setTitle("Blood Bank Management System - Dashboard");
        setSize(950, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUI();
        refreshStats();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND);

        // ---- Header ----
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.PRIMARY);
        header.setPreferredSize(new Dimension(0, 70));
        JLabel title = new JLabel("  Blood Bank Management System");
        title.setForeground(Color.WHITE);
        title.setFont(UIConstants.FONT_TITLE);
        header.add(title, BorderLayout.WEST);

        JLabel welcome = new JLabel("Logged in as: " + admin.getUsername() + "  ");
        welcome.setForeground(Color.WHITE);
        welcome.setFont(UIConstants.FONT_LABEL);
        header.add(welcome, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ---- Center: stats + navigation ----
        JPanel center = new JPanel();
        center.setBackground(UIConstants.BACKGROUND);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        statsPanel.setBackground(UIConstants.BACKGROUND);
        statsPanel.setMaximumSize(new Dimension(1000, 120));

        donorsValue = new JLabel("0");
        unitsValue = new JLabel("0");
        requestsValue = new JLabel("0");
        donationsValue = new JLabel("0");

        statsPanel.add(statCard("Total Donors", donorsValue, UIConstants.PRIMARY));
        statsPanel.add(statCard("Total Blood Units", unitsValue, UIConstants.SUCCESS));
        statsPanel.add(statCard("Total Blood Requests", requestsValue, UIConstants.WARNING));
        statsPanel.add(statCard("Total Donations", donationsValue, UIConstants.PRIMARY_DARK));

        center.add(statsPanel);
        center.add(Box.createRigidArea(new Dimension(0, 25)));

        JLabel navLabel = new JLabel("Navigation");
        navLabel.setFont(UIConstants.FONT_HEADING);
        navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(navLabel);
        center.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel navPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        navPanel.setBackground(UIConstants.BACKGROUND);
        navPanel.setMaximumSize(new Dimension(1000, 220));

        navPanel.add(navCard("Donor Management", e -> new DonorManagementUI().setVisible(true)));
        navPanel.add(navCard("Blood Stock", e -> new BloodStockUI().setVisible(true)));
        navPanel.add(navCard("Donation Management", e -> new DonationUI().setVisible(true)));
        navPanel.add(navCard("Blood Requests", e -> new BloodRequestUI().setVisible(true)));
        navPanel.add(navCard("Search Blood", e -> new SearchBloodUI().setVisible(true)));
        navPanel.add(navCard("Reports", e -> new ReportsUI().setVisible(true)));
        navPanel.add(navCard("Refresh Stats", e -> refreshStats()));
        navPanel.add(navCard("Logout", e -> logout()));

        center.add(navPanel);
        root.add(center, BorderLayout.CENTER);

        setContentPane(root);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                refreshStats();
            }
        });
    }

    private JPanel statCard(String label, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel textLabel = new JLabel(label, SwingConstants.CENTER);
        textLabel.setFont(UIConstants.FONT_LABEL);

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(textLabel, BorderLayout.SOUTH);
        return card;
    }

    private JButton navCard(String label, java.awt.event.ActionListener listener) {
        JButton button = new JButton("<html><center>" + label + "</center></html>");
        button.setFont(UIConstants.FONT_BUTTON);
        button.setBackground(UIConstants.PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.addActionListener(listener);
        return button;
    }

    private void refreshStats() {
        try {
            DashboardService.Stats stats = dashboardService.getStats();
            donorsValue.setText(String.valueOf(stats.totalDonors));
            unitsValue.setText(String.valueOf(stats.totalBloodUnits));
            requestsValue.setText(String.valueOf(stats.totalRequests));
            donationsValue.setText(String.valueOf(stats.totalDonations));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load dashboard statistics:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
