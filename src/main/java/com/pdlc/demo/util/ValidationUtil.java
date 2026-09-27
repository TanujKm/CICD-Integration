package com.pdlc.demo.util;

import java.util.regex.Pattern;

/**
 * Validation helper used by the service layer before persisting data.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.length() <= 50;
    }

    public boolean isValidSalary(double salary) {
        return salary > 0;
    }
}
