package com.pdlc.demo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    void testGettersAndSetters() {
        Employee employee = new Employee(1, "Aditi Sharma", "aditi@example.com", "QA", 50000);

        assertEquals(1, employee.getId());
        assertEquals("Aditi Sharma", employee.getName());
        assertEquals("aditi@example.com", employee.getEmail());
        assertEquals("QA", employee.getDepartment());
        assertEquals(50000, employee.getSalary());
    }

    @Test
    void testSettersUpdateValues() {
        Employee employee = new Employee(1, "Aditi Sharma", "aditi@example.com", "QA", 50000);
        employee.setSalary(60000);
        employee.setDepartment("Engineering");

        assertEquals(60000, employee.getSalary());
        assertEquals("Engineering", employee.getDepartment());
    }

    @Test
    void testEqualsBasedOnId() {
        Employee e1 = new Employee(1, "Aditi Sharma", "aditi@example.com", "QA", 50000);
        Employee e2 = new Employee(1, "Different Name", "different@example.com", "HR", 40000);
        Employee e3 = new Employee(2, "Aditi Sharma", "aditi@example.com", "QA", 50000);

        assertEquals(e1, e2); // same ID -> equal
        assertNotEquals(e1, e3); // different ID -> not equal
    }
}
