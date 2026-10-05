package exception;

/**
 * Custom checked exception used across the DAO/service layers
 * for business-rule violations (insufficient stock, duplicate IDs, etc.)
 */
public class BloodBankException extends Exception {
    public BloodBankException(String message) {
        super(message);
    }

    public BloodBankException(String message, Throwable cause) {
        super(message, cause);
    }
}
