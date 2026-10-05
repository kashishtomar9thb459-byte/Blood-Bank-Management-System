package service;

import exception.BloodBankException;

import java.time.LocalDate;
import java.util.Set;
import java.util.regex.Pattern;

/** Shared validation rules used across all service classes. */
public class ValidationUtil {

    private static final Set<String> VALID_BLOOD_GROUPS = Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    public static void requireNonEmpty(String value, String fieldName) throws BloodBankException {
        if (value == null || value.trim().isEmpty()) {
            throw new BloodBankException(fieldName + " cannot be empty.");
        }
    }

    public static void validateAge(int age) throws BloodBankException {
        if (age < 18 || age > 65) {
            throw new BloodBankException("Age must be between 18 and 65 for a donor.");
        }
    }

    public static void validatePhoneNumber(String phone) throws BloodBankException {
        if (phone == null || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new BloodBankException("Phone number must be a valid 10-digit Indian mobile number.");
        }
    }

    public static void validateBloodGroup(String bloodGroup) throws BloodBankException {
        if (bloodGroup == null || !VALID_BLOOD_GROUPS.contains(bloodGroup.trim())) {
            throw new BloodBankException("Invalid blood group selected.");
        }
    }

    public static void validatePositiveUnits(int units) throws BloodBankException {
        if (units <= 0) {
            throw new BloodBankException("Units must be a positive number.");
        }
    }

    public static void validateDateNotFuture(LocalDate date) throws BloodBankException {
        if (date == null) {
            throw new BloodBankException("Date cannot be empty.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new BloodBankException("Date cannot be in the future.");
        }
    }

    public static int parseAge(String text) throws BloodBankException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new BloodBankException("Age must be a valid number.");
        }
    }

    public static int parseUnits(String text) throws BloodBankException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new BloodBankException("Units must be a valid number.");
        }
    }
}
