package dao;

import database.DBConnection;
import model.BloodRequest;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BloodRequestDAO {

    public int addRequest(BloodRequest request) throws SQLException {
        String sql = "INSERT INTO blood_request (patient_name, hospital_name, contact_number, blood_group, "
                + "units_required, request_date, status) VALUES (?,?,?,?,?,?,?)";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, request.getPatientName());
            ps.setString(2, request.getHospitalName());
            ps.setString(3, request.getContactNumber());
            ps.setString(4, request.getBloodGroup());
            ps.setInt(5, request.getUnitsRequired());
            ps.setDate(6, Date.valueOf(request.getRequestDate()));
            ps.setString(7, request.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateStatus(int requestId, String status) throws SQLException {
        String sql = "UPDATE blood_request SET status=? WHERE request_id=?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, requestId);
            return ps.executeUpdate() > 0;
        }
    }

    public BloodRequest getRequestById(int requestId) throws SQLException {
        String sql = "SELECT * FROM blood_request WHERE request_id=?";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<BloodRequest> getAllRequests() throws SQLException {
        List<BloodRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM blood_request ORDER BY request_id DESC";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<BloodRequest> searchByStatus(String status) throws SQLException {
        List<BloodRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM blood_request WHERE status=? ORDER BY request_id DESC";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countRequests() throws SQLException {
        String sql = "SELECT COUNT(*) FROM blood_request";
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private BloodRequest mapRow(ResultSet rs) throws SQLException {
        LocalDate date = rs.getDate("request_date").toLocalDate();
        return new BloodRequest(
                rs.getInt("request_id"),
                rs.getString("patient_name"),
                rs.getString("hospital_name"),
                rs.getString("contact_number"),
                rs.getString("blood_group"),
                rs.getInt("units_required"),
                date,
                rs.getString("status")
        );
    }
}
