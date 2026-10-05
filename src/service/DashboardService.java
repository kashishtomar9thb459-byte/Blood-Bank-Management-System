package service;

import dao.BloodRequestDAO;
import dao.BloodStockDAO;
import dao.DonationDAO;
import dao.DonorDAO;

import java.sql.SQLException;

/** Aggregates the statistics shown on the dashboard, calculated live from the database. */
public class DashboardService {

    private final DonorDAO donorDAO = new DonorDAO();
    private final BloodStockDAO stockDAO = new BloodStockDAO();
    private final DonationDAO donationDAO = new DonationDAO();
    private final BloodRequestDAO requestDAO = new BloodRequestDAO();

    public static class Stats {
        public int totalDonors;
        public int totalBloodUnits;
        public int totalRequests;
        public int totalDonations;
    }

    public Stats getStats() throws SQLException {
        Stats stats = new Stats();
        stats.totalDonors = donorDAO.countDonors();
        stats.totalBloodUnits = stockDAO.getTotalUnits();
        stats.totalRequests = requestDAO.countRequests();
        stats.totalDonations = donationDAO.countDonations();
        return stats;
    }
}
