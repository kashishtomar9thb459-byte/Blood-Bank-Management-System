package dao;

import database.DBConnection;
import model.BloodStock;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BloodStockDAO {

    public List<BloodStock> getAllStock() throws SQLException {
        List<BloodStock> list = new ArrayList<>();
        String sql = "SELECT * FROM blood_stock ORDER BY blood_group";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new BloodStock(rs.getString("blood_group"), rs.getInt("units_available")));
            }
        }
        return list;
    }

    public BloodStock getStockByGroup(String bloodGroup) throws SQLException {
        String sql = "SELECT * FROM blood_stock WHERE blood_group=?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bloodGroup);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new BloodStock(rs.getString("blood_group"), rs.getInt("units_available"));
            }
        }
        return null;
    }

    /** Increases stock by the given number of units (used when a donation is recorded). */
    public boolean increaseStock(String bloodGroup, int units) throws SQLException {
        String sql = "UPDATE blood_stock SET units_available = units_available + ? WHERE blood_group=?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, units);
            ps.setString(2, bloodGroup);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Decreases stock by the given number of units (used when a request is approved/completed).
     * Guarded at the SQL level so units_available can never go negative.
     */
    public boolean decreaseStock(String bloodGroup, int units) throws SQLException {
        String sql = "UPDATE blood_stock SET units_available = units_available - ? "
                + "WHERE blood_group=? AND units_available >= ?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, units);
            ps.setString(2, bloodGroup);
            ps.setInt(3, units);
            return ps.executeUpdate() > 0;
        }
    }

    public int getTotalUnits() throws SQLException {
        String sql = "SELECT SUM(units_available) FROM blood_stock";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
