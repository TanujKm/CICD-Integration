package com.pdlc.demo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    private final ValidationUtil validationUtil = new ValidationUtil();

    @Test
    void testValidEmailIsAccepted() {
        assertTrue(validationUtil.isValidEmail("test.user@example.com"));
    }

    @Test
    void testEmailWithoutAtSymbolIsRejected() {
        assertFalse(validationUtil.isValidEmail("test.user-example.com"));
    }

    @Test
    void testNullOrBlankEmailIsRejected() {
        assertFalse(validationUtil.isValidEmail(null));
        assertFalse(validationUtil.isValidEmail("  "));
    }

    @Test
    void testValidNameIsAccepted() {
        assertTrue(validationUtil.isValidName("Rohan Mehta"));
    }

    @Test
    void testBlankNameIsRejected() {
        assertFalse(validationUtil.isValidName(""));
        assertFalse(validationUtil.isValidName(null));
    }

    @Test
    void testPositiveSalaryIsValid() {
        assertTrue(validationUtil.isValidSalary(50000));
    }

    @Test
    void testZeroOrNegativeSalaryIsInvalid() {
        assertFalse(validationUtil.isValidSalary(0));
        assertFalse(validationUtil.isValidSalary(-500));
    }
}
