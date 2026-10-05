package model;

import java.time.LocalDate;

public class Donation {
    private int donationId;
    private int donorId;
    private String donorName;   // convenience field for display/reports
    private String bloodGroup;
    private LocalDate donationDate;
    private int unitsDonated;

    public Donation() {}

    public Donation(int donationId, int donorId, String donorName, String bloodGroup,
                     LocalDate donationDate, int unitsDonated) {
        this.donationId = donationId;
        this.donorId = donorId;
        this.donorName = donorName;
        this.bloodGroup = bloodGroup;
        this.donationDate = donationDate;
        this.unitsDonated = unitsDonated;
    }

    public int getDonationId() { return donationId; }
    public void setDonationId(int donationId) { this.donationId = donationId; }

    public int getDonorId() { return donorId; }
    public void setDonorId(int donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public LocalDate getDonationDate() { return donationDate; }
    public void setDonationDate(LocalDate donationDate) { this.donationDate = donationDate; }

    public int getUnitsDonated() { return unitsDonated; }
    public void setUnitsDonated(int unitsDonated) { this.unitsDonated = unitsDonated; }
}
