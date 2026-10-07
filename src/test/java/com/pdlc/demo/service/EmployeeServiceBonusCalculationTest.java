package com.pdlc.demo.service;

import com.pdlc.demo.model.Employee;
import com.pdlc.demo.repository.EmployeeRepository;
import com.pdlc.demo.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceBonusCalculationTest {

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(new EmployeeRepository(), new ValidationUtil());
    }

    @Test
    void testCalculateBonusAtTenPercent() {
        Employee employee = new Employee(1, "Aditi Sharma", "aditi@example.com", "QA", 50000);

        double bonus = employeeService.calculateAnnualBonus(employee, 10);

        assertEquals(5000, bonus);
    }

    @Test
    void testCalculateBonusAtZeroPercent() {
        Employee employee = new Employee(1, "Aditi Sharma", "aditi@example.com", "QA", 50000);

        double bonus = employeeService.calculateAnnualBonus(employee, 0);

        assertEquals(0, bonus);
    }

    @Test
    void testCalculateBonusRejectsNegativePercentage() {
        Employee employee = new Employee(1, "Aditi Sharma", "aditi@example.com", "QA", 50000);

        assertThrows(IllegalArgumentException.class,
                () -> employeeService.calculateAnnualBonus(employee, -5));
    }

    @Test
    void testCalculateBonusRejectsOverFiftyPercent() {
        Employee employee = new Employee(1, "Aditi Sharma", "aditi@example.com", "QA", 50000);

        assertThrows(IllegalArgumentException.class,
                () -> employeeService.calculateAnnualBonus(employee, 75));
    }

    @Test
    void testCalculateBonusRejectsNullEmployee() {
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.calculateAnnualBonus(null, 10));
    }
}
