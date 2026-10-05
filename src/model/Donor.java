package model;

import java.time.LocalDate;

public class Donor {
    private int donorId;
    private String fullName;
    private int age;
    private String gender;
    private String bloodGroup;
    private String phoneNumber;
    private String email;
    private String address;
    private LocalDate lastDonationDate;

    public Donor() {}

    public Donor(int donorId, String fullName, int age, String gender, String bloodGroup,
                 String phoneNumber, String email, String address, LocalDate lastDonationDate) {
        this.donorId = donorId;
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.lastDonationDate = lastDonationDate;
    }

    // Constructor without id, used before insertion
    public Donor(String fullName, int age, String gender, String bloodGroup,
                 String phoneNumber, String email, String address, LocalDate lastDonationDate) {
        this(-1, fullName, age, gender, bloodGroup, phoneNumber, email, address, lastDonationDate);
    }

    public int getDonorId() { return donorId; }
    public void setDonorId(int donorId) { this.donorId = donorId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    @Override
    public String toString() {
        return fullName + " (" + bloodGroup + ")";
    }
}
