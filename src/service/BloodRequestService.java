package service;

import dao.BloodRequestDAO;
import exception.BloodBankException;
import model.BloodRequest;

import java.sql.SQLException;
import java.util.List;

public class BloodRequestService {

    private final BloodRequestDAO requestDAO = new BloodRequestDAO();
    private final BloodStockService stockService = new BloodStockService();

    public int addRequest(BloodRequest request) throws BloodBankException, SQLException {
        validate(request);
        request.setStatus("Pending");
        return requestDAO.addRequest(request);
    }

    /**
     * Approves (or completes) a request. Before changing status, checks that enough
     * blood is available; if so, decreases stock and updates the status.
     * Rejecting a request never touches stock.
     */
    public void updateStatus(int requestId, String newStatus) throws BloodBankException, SQLException {
        BloodRequest request = requestDAO.getRequestById(requestId);
        if (request == null) {
            throw new BloodBankException("Request ID " + requestId + " does not exist.");
        }

        boolean movesToStockDeductingState =
                ("Approved".equals(newStatus) || "Completed".equals(newStatus))
                        && !"Approved".equals(request.getStatus())
                        && !"Completed".equals(request.getStatus());

        if (movesToStockDeductingState) {
            if (!stockService.hasSufficientStock(request.getBloodGroup(), request.getUnitsRequired())) {
                throw new BloodBankException("Cannot approve request: insufficient stock of "
                        + request.getBloodGroup() + ".");
            }
            stockService.decreaseStock(request.getBloodGroup(), request.getUnitsRequired());
        }

        requestDAO.updateStatus(requestId, newStatus);
    }

    public List<BloodRequest> getAllRequests() throws SQLException {
        return requestDAO.getAllRequests();
    }

    public List<BloodRequest> searchByStatus(String status) throws SQLException {
        return requestDAO.searchByStatus(status);
    }

    public int countRequests() throws SQLException {
        return requestDAO.countRequests();
    }

    private void validate(BloodRequest r) throws BloodBankException {
        ValidationUtil.requireNonEmpty(r.getPatientName(), "Patient name");
        ValidationUtil.requireNonEmpty(r.getHospitalName(), "Hospital name");
        ValidationUtil.validatePhoneNumber(r.getContactNumber());
        ValidationUtil.validateBloodGroup(r.getBloodGroup());
        ValidationUtil.validatePositiveUnits(r.getUnitsRequired());
        if (r.getRequestDate() == null) {
            throw new BloodBankException("Request date cannot be empty.");
        }
    }
}
