package com.example.shopapp.utils;

import java.util.regex.Pattern;

/**
 * Utility class for validating user input such as email, phone number, and password.
 */
public final class ValidationUtils {

    // Prevent instantiation
    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    // Precompiled safe and efficient pattern for email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}$"
    );

    // Precompiled pattern for phone number validation (digits only, at least 6 digits)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{6,}$");

    /**
     * Validates an email address.
     *
     * @param email The email string to validate.
     * @return true if the email is valid, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates a phone number.
     *
     * @param phoneNumber The phone number string to validate.
     * @return true if the phone number is valid, false otherwise.
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    /**
     * Validates a password.
     *
     * @param password The password string to validate.
     * @return true if the password has at least 3 characters, false otherwise.
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 3;
    }
}
