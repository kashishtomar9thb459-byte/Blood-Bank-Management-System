package dao;

import database.DBConnection;
import model.Donation;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DonationDAO {

    public int addDonation(Donation donation) throws SQLException {
        String sql = "INSERT INTO donation (donor_id, blood_group, donation_date, units_donated) VALUES (?,?,?,?)";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, donation.getDonorId());
            ps.setString(2, donation.getBloodGroup());
            ps.setDate(3, Date.valueOf(donation.getDonationDate()));
            ps.setInt(4, donation.getUnitsDonated());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<Donation> getAllDonations() throws SQLException {
        List<Donation> list = new ArrayList<>();
        String sql = "SELECT d.donation_id, d.donor_id, dn.full_name, d.blood_group, d.donation_date, d.units_donated "
                + "FROM donation d JOIN donor dn ON d.donor_id = dn.donor_id ORDER BY d.donation_id DESC";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Donation> searchByDonorName(String name) throws SQLException {
        List<Donation> list = new ArrayList<>();
        String sql = "SELECT d.donation_id, d.donor_id, dn.full_name, d.blood_group, d.donation_date, d.units_donated "
                + "FROM donation d JOIN donor dn ON d.donor_id = dn.donor_id "
                + "WHERE dn.full_name LIKE ? ORDER BY d.donation_id DESC";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countDonations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM donation";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Donation mapRow(ResultSet rs) throws SQLException {
        LocalDate date = rs.getDate("donation_date").toLocalDate();
        return new Donation(
                rs.getInt("donation_id"),
                rs.getInt("donor_id"),
                rs.getString("full_name"),
                rs.getString("blood_group"),
                date,
                rs.getInt("units_donated")
        );
    }
}
