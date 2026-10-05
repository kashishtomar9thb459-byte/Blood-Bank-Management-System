package model;

public class BloodStock {
    private String bloodGroup;
    private int unitsAvailable;

    public BloodStock() {}

    public BloodStock(String bloodGroup, int unitsAvailable) {
        this.bloodGroup = bloodGroup;
        this.unitsAvailable = unitsAvailable;
    }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public int getUnitsAvailable() { return unitsAvailable; }
    public void setUnitsAvailable(int unitsAvailable) { this.unitsAvailable = unitsAvailable; }

    /** Business rule: >5 = Available, 1-5 = Low Stock, 0 = Not Available */
    public String getStatus() {
        if (unitsAvailable <= 0) return "Not Available";
        if (unitsAvailable <= 5) return "Low Stock";
        return "Available";
    }
}
