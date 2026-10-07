package com.pdlc.demo.service;

import com.pdlc.demo.model.Employee;
import com.pdlc.demo.repository.EmployeeRepository;
import com.pdlc.demo.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceTest {
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        EmployeeRepository repository = new EmployeeRepository();
        ValidationUtil validationUtil = new ValidationUtil();
        employeeService = new EmployeeService(repository, validationUtil);
    }

    @Test
    void testAddEmployeeSucceedsWithValidData() {
        Employee saved = employeeService.addEmployee("Aditi Sharma", "aditi@example.com", "QA", 50000);

        assertNotNull(saved);
        // assertEquals("Aditi Sharma", saved.getName());
                assertEquals("Adi", saved.getName());

    }

    @Test
    void testAddEmployeeFailsWithInvalidEmail() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.addEmployee("Aditi Sharma", "not-an-email", "QA", 50000));

        assertEquals("Invalid email address", ex.getMessage());
    }

    @Test
    void testAddEmployeeFailsWithDuplicateEmail() {
        employeeService.addEmployee("Aditi Sharma", "aditi@example.com", "QA", 50000);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.addEmployee("Different Name", "aditi@example.com", "HR", 40000));

        assertEquals("An employee with this email already exists", ex.getMessage());
    }

    @Test
    void testAddEmployeeFailsWithZeroSalary() {
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.addEmployee("Aditi Sharma", "aditi@example.com", "QA", 0));
    }

    @Test
    void testGetAllEmployeesSortedByName() {
     
        employeeService.addEmployee("Rohan Mehta", "rohan@example.com", "Engineering", 60000);
        employeeService.addEmployee("Aditi Sharma", "aditi@example.com", "QA", 50000);

        List<Employee> sorted = employeeService.getAllEmployeesSortedByName();

        assertEquals("Aditi Sharma", sorted.get(0).getName());
        assertEquals("Rohan Mehta", sorted.get(1).getName());
    }

    @Test
    void testDeleteEmployeeReturnsTrueWhenRemoved() {
        Employee saved = employeeService.addEmployee("Aditi Sharma", "aditi@example.com", "QA", 50000);

        assertTrue(employeeService.deleteEmployee(saved.getId()));
    }
}
