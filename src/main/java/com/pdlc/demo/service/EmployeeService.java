package com.pdlc.demo.service;

import com.pdlc.demo.model.Employee;
import com.pdlc.demo.repository.EmployeeRepository;
import com.pdlc.demo.util.ValidationUtil;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic layer — validates input, enforces business rules,
 * and coordinates with the repository layer.
 */
public class EmployeeService {

    private final EmployeeRepository repository;
    private final ValidationUtil validationUtil;

    public EmployeeService(EmployeeRepository repository, ValidationUtil validationUtil) {
        this.repository = repository;
        this.validationUtil = validationUtil;
    }

    public Employee addEmployee(String name, String email, String department, double salary) {
        if (!validationUtil.isValidName(name)) {
            throw new IllegalArgumentException("Invalid employee name");
        }
        if (!validationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address");
        }
        if (!validationUtil.isValidSalary(salary)) {
            throw new IllegalArgumentException("Salary must be greater than zero");
        }
        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException("An employee with this email already exists");
        }

        Employee employee = new Employee(0, name, email, department, salary);
        return repository.save(employee);
    }

    public List<Employee> getAllEmployeesSortedByName() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Employee::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public boolean deleteEmployee(int id) {
        return repository.deleteById(id);
    }

    /**
     * Calculates annual bonus for an employee based on a percentage of salary.
     * Business rule: bonus percentage must be between 0 and 50 (inclusive).
     */
    public double calculateAnnualBonus(Employee employee, double bonusPercentage) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (bonusPercentage < 0 || bonusPercentage > 50) {
            throw new IllegalArgumentException("Bonus percentage must be between 0 and 50");
        }
        return employee.getSalary() * (bonusPercentage / 100.0);
    }
}
