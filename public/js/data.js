/**
 * Blood Bank Management System - Data Storage Layer
 * Manages persistence in localStorage with default seed data matching database/schema.sql
 */

const SEED_DATA = {
  admin: {
    username: "admin",
    password: "admin123",
    name: "System Administrator"
  },
  bloodStock: {
    "A+": 10,
    "A-": 5,
    "B+": 10,
    "B-": 5,
    "AB+": 5,
    "AB-": 2,
    "O+": 15,
    "O-": 8
  },
  donors: [
    {
      donorId: 1,
      fullName: "Rahul Sharma",
      age: 28,
      gender: "Male",
      bloodGroup: "O+",
      phoneNumber: "9876543210",
      email: "rahul.sharma@example.com",
      address: "Indore, MP",
      lastDonationDate: "2026-06-15"
    },
    {
      donorId: 2,
      fullName: "Priya Verma",
      age: 24,
      gender: "Female",
      bloodGroup: "A+",
      phoneNumber: "9123456780",
      email: "priya.verma@example.com",
      address: "Bhopal, MP",
      lastDonationDate: "2026-05-10"
    },
    {
      donorId: 3,
      fullName: "Amit Singh",
      age: 32,
      gender: "Male",
      bloodGroup: "B+",
      phoneNumber: "9988776655",
      email: "amit.singh@example.com",
      address: "Ujjain, MP",
      lastDonationDate: "2026-07-01"
    },
    {
      donorId: 4,
      fullName: "Sneha Patil",
      age: 27,
      gender: "Female",
      bloodGroup: "AB+",
      phoneNumber: "9090909090",
      email: "sneha.patil@example.com",
      address: "Indore, MP",
      lastDonationDate: null
    }
  ],
  donations: [
    {
      donationId: 1,
      donorId: 1,
      donorName: "Rahul Sharma",
      bloodGroup: "O+",
      donationDate: "2026-06-15",
      unitsDonated: 1
    },
    {
      donationId: 2,
      donorId: 2,
      donorName: "Priya Verma",
      bloodGroup: "A+",
      donationDate: "2026-05-10",
      unitsDonated: 1
    },
    {
      donationId: 3,
      donorId: 3,
      donorName: "Amit Singh",
      bloodGroup: "B+",
      donationDate: "2026-07-01",
      unitsDonated: 1
    }
  ],
  bloodRequests: [
    {
      requestId: 1,
      patientName: "Ramesh Chandra",
      hospitalName: "City Hospital",
      contactNumber: "9871234560",
      bloodGroup: "O+",
      unitsRequired: 2,
      requestDate: "2026-09-01",
      status: "Pending"
    },
    {
      requestId: 2,
      patientName: "Kavita Joshi",
      hospitalName: "Apollo Clinic",
      contactNumber: "9112233445",
      bloodGroup: "A+",
      unitsRequired: 1,
      requestDate: "2026-09-10",
      status: "Approved"
    }
  ]
};

const DB_KEY = "blood_bank_db_v1";

class BloodBankDB {
  constructor() {
    this.memoryData = null;
    this.init();
  }

  init() {
    try {
      const existing = localStorage.getItem(DB_KEY);
      if (!existing) {
        this.resetToDefaults();
      } else {
        this.memoryData = JSON.parse(existing);
      }
    } catch (e) {
      console.warn("localStorage not available, using in-memory store", e);
      this.resetToDefaults();
    }
  }

  getData() {
    try {
      const raw = localStorage.getItem(DB_KEY);
      if (raw) {
        this.memoryData = JSON.parse(raw);
        return this.memoryData;
      }
    } catch (e) {}
    if (!this.memoryData) {
      this.resetToDefaults();
    }
    return this.memoryData;
  }

  saveData(data) {
    this.memoryData = data;
    try {
      localStorage.setItem(DB_KEY, JSON.stringify(data));
    } catch (e) {
      console.warn("Could not save to localStorage, using in-memory fallback", e);
    }
  }

  resetToDefaults() {
    const clone = JSON.parse(JSON.stringify(SEED_DATA));
    this.memoryData = clone;
    try {
      localStorage.setItem(DB_KEY, JSON.stringify(clone));
    } catch (e) {}
    return clone;
  }

  // --- Auth ---
  authenticate(username, password) {
    const data = this.getData();
    if (data.admin.username === username && data.admin.password === password) {
      return { username: data.admin.username, name: data.admin.name };
    }
    return null;
  }

  updateAdminPassword(newPassword) {
    const data = this.getData();
    data.admin.password = newPassword;
    this.saveData(data);
    return true;
  }

  // --- Donors ---
  getAllDonors() {
    return this.getData().donors;
  }

  getDonorById(id) {
    return this.getData().donors.find(d => d.donorId === Number(id)) || null;
  }

  addDonor(donor) {
    const data = this.getData();
    const nextId = data.donors.length > 0 ? Math.max(...data.donors.map(d => d.donorId)) + 1 : 1;
    const newDonor = {
      ...donor,
      donorId: nextId
    };
    data.donors.push(newDonor);
    this.saveData(data);
    return newDonor;
  }

  updateDonor(donor) {
    const data = this.getData();
    const index = data.donors.findIndex(d => d.donorId === Number(donor.donorId));
    if (index === -1) throw new Error("Donor ID " + donor.donorId + " does not exist.");
    data.donors[index] = { ...data.donors[index], ...donor };
    this.saveData(data);
    return data.donors[index];
  }

  deleteDonor(donorId) {
    const data = this.getData();
    const index = data.donors.findIndex(d => d.donorId === Number(donorId));
    if (index === -1) throw new Error("Donor ID " + donorId + " does not exist.");
    const deleted = data.donors.splice(index, 1)[0];
    this.saveData(data);
    return deleted;
  }

  // --- Blood Stock ---
  getAllStock() {
    const data = this.getData();
    const result = [];
    const groups = ["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"];
    for (const g of groups) {
      const units = data.bloodStock[g] || 0;
      let status = "Available";
      if (units === 0) status = "Not Available";
      else if (units < 5) status = "Low Stock";
      result.push({ bloodGroup: g, unitsAvailable: units, status });
    }
    return result;
  }

  getStockByGroup(bloodGroup) {
    const data = this.getData();
    const units = data.bloodStock[bloodGroup] || 0;
    let status = "Available";
    if (units === 0) status = "Not Available";
    else if (units < 5) status = "Low Stock";
    return { bloodGroup, unitsAvailable: units, status };
  }

  increaseStock(bloodGroup, units) {
    const data = this.getData();
    data.bloodStock[bloodGroup] = (data.bloodStock[bloodGroup] || 0) + units;
    this.saveData(data);
    return data.bloodStock[bloodGroup];
  }

  decreaseStock(bloodGroup, units) {
    const data = this.getData();
    const current = data.bloodStock[bloodGroup] || 0;
    if (current < units) {
      throw new Error(`Insufficient blood stock for ${bloodGroup}. Available: ${current} unit(s), requested: ${units} unit(s).`);
    }
    data.bloodStock[bloodGroup] = current - units;
    this.saveData(data);
    return data.bloodStock[bloodGroup];
  }

  hasSufficientStock(bloodGroup, units) {
    const current = this.getData().bloodStock[bloodGroup] || 0;
    return current >= units;
  }

  getTotalUnits() {
    const data = this.getData();
    return Object.values(data.bloodStock).reduce((sum, u) => sum + u, 0);
  }

  // --- Donations ---
  getAllDonations() {
    return this.getData().donations;
  }

  recordDonation({ donorId, bloodGroup, donationDate, unitsDonated }) {
    const data = this.getData();
    const donor = data.donors.find(d => d.donorId === Number(donorId));
    if (!donor) throw new Error("Selected donor does not exist.");

    const nextId = data.donations.length > 0 ? Math.max(...data.donations.map(d => d.donationId)) + 1 : 1;
    const donation = {
      donationId: nextId,
      donorId: Number(donorId),
      donorName: donor.fullName,
      bloodGroup: bloodGroup || donor.bloodGroup,
      donationDate,
      unitsDonated: Number(unitsDonated)
    };

    data.donations.unshift(donation);

    // Increase blood stock (FR5 rule)
    data.bloodStock[donation.bloodGroup] = (data.bloodStock[donation.bloodGroup] || 0) + Number(unitsDonated);

    // Update donor's last donation date
    donor.lastDonationDate = donationDate;

    this.saveData(data);
    return donation;
  }

  // --- Blood Requests ---
  getAllRequests() {
    return this.getData().bloodRequests;
  }

  getRequestById(requestId) {
    return this.getData().bloodRequests.find(r => r.requestId === Number(requestId)) || null;
  }

  addRequest({ patientName, hospitalName, contactNumber, bloodGroup, unitsRequired, requestDate }) {
    const data = this.getData();
    const nextId = data.bloodRequests.length > 0 ? Math.max(...data.bloodRequests.map(r => r.requestId)) + 1 : 1;
    const newRequest = {
      requestId: nextId,
      patientName,
      hospitalName,
      contactNumber,
      bloodGroup,
      unitsRequired: Number(unitsRequired),
      requestDate,
      status: "Pending"
    };
    data.bloodRequests.unshift(newRequest);
    this.saveData(data);
    return newRequest;
  }

  updateRequestStatus(requestId, newStatus) {
    const data = this.getData();
    const request = data.bloodRequests.find(r => r.requestId === Number(requestId));
    if (!request) throw new Error("Request ID " + requestId + " does not exist.");

    const oldStatus = request.status;
    const movesToStockDeductingState =
      (newStatus === "Approved" || newStatus === "Completed") &&
      oldStatus !== "Approved" &&
      oldStatus !== "Completed";

    if (movesToStockDeductingState) {
      const currentStock = data.bloodStock[request.bloodGroup] || 0;
      if (currentStock < request.unitsRequired) {
        throw new Error(
          `Cannot approve request: insufficient stock of ${request.bloodGroup}. Available: ${currentStock} unit(s), Required: ${request.unitsRequired} unit(s).`
        );
      }
      data.bloodStock[request.bloodGroup] = currentStock - request.unitsRequired;
    }

    request.status = newStatus;
    this.saveData(data);
    return request;
  }

  // --- Stats for Dashboard ---
  getDashboardStats() {
    const data = this.getData();
    const totalDonors = data.donors.length;
    const totalBloodUnits = Object.values(data.bloodStock).reduce((sum, u) => sum + u, 0);
    const totalRequests = data.bloodRequests.length;
    const totalDonations = data.donations.length;

    const pendingRequests = data.bloodRequests.filter(r => r.status === "Pending").length;
    const lowStockCount = Object.values(data.bloodStock).filter(u => u > 0 && u < 5).length;
    const outOfStockCount = Object.values(data.bloodStock).filter(u => u === 0).length;

    return {
      totalDonors,
      totalBloodUnits,
      totalRequests,
      totalDonations,
      pendingRequests,
      lowStockCount,
      outOfStockCount
    };
  }

  // --- Export / Import ---
  exportJSON() {
    return JSON.stringify(this.getData(), null, 2);
  }

  importJSON(jsonString) {
    try {
      const parsed = JSON.parse(jsonString);
      if (!parsed.bloodStock || !parsed.donors) {
        throw new Error("Invalid Blood Bank data structure.");
      }
      this.saveData(parsed);
      return true;
    } catch (e) {
      throw new Error("Failed to import database: " + e.message);
    }
  }
}

// Export singleton instance
window.db = new BloodBankDB();
