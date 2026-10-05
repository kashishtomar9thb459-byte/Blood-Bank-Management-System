package service;

import dao.DonationDAO;
import exception.BloodBankException;
import model.Donation;

import java.sql.SQLException;
import java.util.List;

public class DonationService {

    private final DonationDAO donationDAO = new DonationDAO();
    private final BloodStockService stockService = new BloodStockService();

    /**
     * Records a donation and automatically increases the corresponding blood stock.
     * Both operations are treated as one logical unit of work at the service layer.
     */
    public int recordDonation(Donation donation) throws BloodBankException, SQLException {
        validate(donation);
        int id = donationDAO.addDonation(donation);
        stockService.increaseStock(donation.getBloodGroup(), donation.getUnitsDonated());
        return id;
    }

    public List<Donation> getAllDonations() throws SQLException {
        return donationDAO.getAllDonations();
    }

    public List<Donation> searchByDonorName(String name) throws SQLException {
        return donationDAO.searchByDonorName(name);
    }

    public int countDonations() throws SQLException {
        return donationDAO.countDonations();
    }

    private void validate(Donation d) throws BloodBankException {
        if (d.getDonorId() <= 0) {
            throw new BloodBankException("A valid donor must be selected.");
        }
        ValidationUtil.validateBloodGroup(d.getBloodGroup());
        ValidationUtil.validatePositiveUnits(d.getUnitsDonated());
        ValidationUtil.validateDateNotFuture(d.getDonationDate());
    }
}
