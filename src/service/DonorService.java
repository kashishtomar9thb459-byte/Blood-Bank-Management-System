package service;

import dao.DonorDAO;
import exception.BloodBankException;
import model.Donor;

import java.sql.SQLException;
import java.util.List;

public class DonorService {

    private final DonorDAO donorDAO = new DonorDAO();

    public int addDonor(Donor donor) throws BloodBankException, SQLException {
        validate(donor);
        return donorDAO.addDonor(donor);
    }

    public void updateDonor(Donor donor) throws BloodBankException, SQLException {
        validate(donor);
        if (donorDAO.getDonorById(donor.getDonorId()) == null) {
            throw new BloodBankException("Donor ID " + donor.getDonorId() + " does not exist.");
        }
        donorDAO.updateDonor(donor);
    }

    public void deleteDonor(int donorId) throws BloodBankException, SQLException {
        if (donorDAO.getDonorById(donorId) == null) {
            throw new BloodBankException("Donor ID " + donorId + " does not exist.");
        }
        donorDAO.deleteDonor(donorId);
    }

    public List<Donor> getAllDonors() throws SQLException {
        return donorDAO.getAllDonors();
    }

    public List<Donor> searchByName(String name) throws SQLException {
        return donorDAO.searchByName(name);
    }

    public List<Donor> searchByBloodGroup(String bloodGroup) throws SQLException {
        return donorDAO.searchByBloodGroup(bloodGroup);
    }

    public Donor getDonorById(int donorId) throws SQLException {
        return donorDAO.getDonorById(donorId);
    }

    public int countDonors() throws SQLException {
        return donorDAO.countDonors();
    }

    private void validate(Donor d) throws BloodBankException {
        ValidationUtil.requireNonEmpty(d.getFullName(), "Full name");
        ValidationUtil.validateAge(d.getAge());
        ValidationUtil.requireNonEmpty(d.getGender(), "Gender");
        ValidationUtil.validateBloodGroup(d.getBloodGroup());
        ValidationUtil.validatePhoneNumber(d.getPhoneNumber());
        if (d.getLastDonationDate() != null) {
            ValidationUtil.validateDateNotFuture(d.getLastDonationDate());
        }
    }
}
