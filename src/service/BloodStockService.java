package service;

import dao.BloodStockDAO;
import exception.BloodBankException;
import model.BloodStock;

import java.sql.SQLException;
import java.util.List;

public class BloodStockService {

    private final BloodStockDAO stockDAO = new BloodStockDAO();

    public List<BloodStock> getAllStock() throws SQLException {
        return stockDAO.getAllStock();
    }

    public BloodStock getStockByGroup(String bloodGroup) throws SQLException {
        return stockDAO.getStockByGroup(bloodGroup);
    }

    public void increaseStock(String bloodGroup, int units) throws SQLException, BloodBankException {
        ValidationUtil.validatePositiveUnits(units);
        stockDAO.increaseStock(bloodGroup, units);
    }

    /** Throws BloodBankException if there is not enough stock to fulfil the request. */
    public void decreaseStock(String bloodGroup, int units) throws SQLException, BloodBankException {
        ValidationUtil.validatePositiveUnits(units);
        BloodStock current = stockDAO.getStockByGroup(bloodGroup);
        if (current == null || current.getUnitsAvailable() < units) {
            throw new BloodBankException("Insufficient blood stock for " + bloodGroup
                    + ". Available: " + (current == null ? 0 : current.getUnitsAvailable()) + " unit(s).");
        }
        boolean success = stockDAO.decreaseStock(bloodGroup, units);
        if (!success) {
            throw new BloodBankException("Stock update failed - insufficient units or concurrent update.");
        }
    }

    public boolean hasSufficientStock(String bloodGroup, int units) throws SQLException {
        BloodStock current = stockDAO.getStockByGroup(bloodGroup);
        return current != null && current.getUnitsAvailable() >= units;
    }

    public int getTotalUnits() throws SQLException {
        return stockDAO.getTotalUnits();
    }
}
