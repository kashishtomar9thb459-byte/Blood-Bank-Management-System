/**
 * Blood Bank Management System - Validation Layer
 * Replicates the exact rules from service/ValidationUtil.java
 */

const VALID_BLOOD_GROUPS = ["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"];
const PHONE_REGEX = /^[6-9]\d{9}$/;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const ValidationUtil = {
  requireNonEmpty(value, fieldName) {
    if (!value || typeof value !== "string" || value.trim().length === 0) {
      throw new Error(`${fieldName} cannot be empty.`);
    }
  },

  validateAge(age) {
    const num = Number(age);
    if (isNaN(num) || !Number.isInteger(num)) {
      throw new Error("Age must be a valid whole number.");
    }
    if (num < 18 || num > 65) {
      throw new Error("Age must be between 18 and 65 for a donor.");
    }
    return num;
  },

  validatePhoneNumber(phone) {
    if (!phone || typeof phone !== "string") {
      throw new Error("Phone number cannot be empty.");
    }
    const clean = phone.trim();
    if (!PHONE_REGEX.test(clean)) {
      throw new Error("Phone number must be a valid 10-digit Indian mobile number (starting with 6, 7, 8, or 9).");
    }
    return clean;
  },

  validateEmail(email) {
    if (!email || email.trim() === "") return "";
    const clean = email.trim();
    if (!EMAIL_REGEX.test(clean)) {
      throw new Error("Please enter a valid email address.");
    }
    return clean;
  },

  validateBloodGroup(bloodGroup) {
    if (!bloodGroup || !VALID_BLOOD_GROUPS.includes(bloodGroup.trim())) {
      throw new Error("Invalid blood group selected. Must be one of: " + VALID_BLOOD_GROUPS.join(", "));
    }
    return bloodGroup.trim();
  },

  validatePositiveUnits(units) {
    const num = Number(units);
    if (isNaN(num) || !Number.isInteger(num) || num <= 0) {
      throw new Error("Units must be a positive integer greater than 0.");
    }
    return num;
  },

  validateDateNotFuture(dateStr) {
    if (!dateStr || dateStr.trim().length === 0) {
      throw new Error("Date cannot be empty.");
    }
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) {
      throw new Error("Invalid date format.");
    }
    const today = new Date();
    today.setHours(23, 59, 59, 999);
    if (date > today) {
      throw new Error("Date cannot be in the future.");
    }
    return dateStr;
  }
};

window.ValidationUtil = ValidationUtil;
window.VALID_BLOOD_GROUPS = VALID_BLOOD_GROUPS;
