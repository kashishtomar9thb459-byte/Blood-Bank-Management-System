package ui;

import model.BloodStock;
import service.BloodStockService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class BloodStockUI extends JFrame {

    private final BloodStockService stockService = new BloodStockService();
    private JTable table;
    private DefaultTableModel tableModel;

    public BloodStockUI() {
        setTitle("Blood Bank Management System - Blood Stock");
        setSize(650, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        loadStock();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UIConstants.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Blood Stock Overview");
        title.setFont(UIConstants.FONT_TITLE);
        root.add(title, BorderLayout.NORTH);

        String[] columns = {"Blood Group", "Available Units", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UIConstants.FONT_TABLE_HEADER);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(UIConstants.BACKGROUND);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(UIConstants.PRIMARY);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadStock());
        bottom.add(refreshBtn);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void loadStock() {
        try {
            List<BloodStock> stockList = stockService.getAllStock();
            tableModel.setRowCount(0);
            for (BloodStock s : stockList) {
                tableModel.addRow(new Object[]{s.getBloodGroup(), s.getUnitsAvailable(), s.getStatus()});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
