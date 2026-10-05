package dao;

import database.DBConnection;
import model.Donor;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DonorDAO {

    public int addDonor(Donor donor) throws SQLException {
        String sql = "INSERT INTO donor (full_name, age, gender, blood_group, phone_number, email, address, last_donation_date) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, donor);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateDonor(Donor donor) throws SQLException {
        String sql = "UPDATE donor SET full_name=?, age=?, gender=?, blood_group=?, phone_number=?, "
                + "email=?, address=?, last_donation_date=? WHERE donor_id=?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            fillStatement(ps, donor);
            ps.setInt(9, donor.getDonorId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteDonor(int donorId) throws SQLException {
        String sql = "DELETE FROM donor WHERE donor_id=?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, donorId);
            return ps.executeUpdate() > 0;
        }
    }

    public Donor getDonorById(int donorId) throws SQLException {
        String sql = "SELECT * FROM donor WHERE donor_id=?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Donor> getAllDonors() throws SQLException {
        List<Donor> list = new ArrayList<>();
        String sql = "SELECT * FROM donor ORDER BY donor_id";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Donor> searchByName(String name) throws SQLException {
        List<Donor> list = new ArrayList<>();
        String sql = "SELECT * FROM donor WHERE full_name LIKE ? ORDER BY donor_id";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Donor> searchByBloodGroup(String bloodGroup) throws SQLException {
        List<Donor> list = new ArrayList<>();
        String sql = "SELECT * FROM donor WHERE blood_group = ? ORDER BY donor_id";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bloodGroup);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countDonors() throws SQLException {
        String sql = "SELECT COUNT(*) FROM donor";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private void fillStatement(PreparedStatement ps, Donor d) throws SQLException {
        ps.setString(1, d.getFullName());
        ps.setInt(2, d.getAge());
        ps.setString(3, d.getGender());
        ps.setString(4, d.getBloodGroup());
        ps.setString(5, d.getPhoneNumber());
        ps.setString(6, d.getEmail());
        ps.setString(7, d.getAddress());
        if (d.getLastDonationDate() != null) {
            ps.setDate(8, Date.valueOf(d.getLastDonationDate()));
        } else {
            ps.setNull(8, Types.DATE);
        }
    }

    private Donor mapRow(ResultSet rs) throws SQLException {
        Date lastDonation = rs.getDate("last_donation_date");
        LocalDate lastDonationDate = (lastDonation != null) ? lastDonation.toLocalDate() : null;
        return new Donor(
                rs.getInt("donor_id"),
                rs.getString("full_name"),
                rs.getInt("age"),
                rs.getString("gender"),
                rs.getString("blood_group"),
                rs.getString("phone_number"),
                rs.getString("email"),
                rs.getString("address"),
                lastDonationDate
        );
    }
}
