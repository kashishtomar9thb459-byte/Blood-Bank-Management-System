package model;

import java.time.LocalDate;

public class BloodRequest {
    private int requestId;
    private String patientName;
    private String hospitalName;
    private String contactNumber;
    private String bloodGroup;
    private int unitsRequired;
    private LocalDate requestDate;
    private String status; // Pending, Approved, Rejected, Completed

    public BloodRequest() {}

    public BloodRequest(int requestId, String patientName, String hospitalName, String contactNumber,
                         String bloodGroup, int unitsRequired, LocalDate requestDate, String status) {
        this.requestId = requestId;
        this.patientName = patientName;
        this.hospitalName = hospitalName;
        this.contactNumber = contactNumber;
        this.bloodGroup = bloodGroup;
        this.unitsRequired = unitsRequired;
        this.requestDate = requestDate;
        this.status = status;
    }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public int getUnitsRequired() { return unitsRequired; }
    public void setUnitsRequired(int unitsRequired) { this.unitsRequired = unitsRequired; }

    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
