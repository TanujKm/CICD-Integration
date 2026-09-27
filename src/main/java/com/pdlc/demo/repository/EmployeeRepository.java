package com.pdlc.demo.repository;

import com.pdlc.demo.model.Employee;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory repository simulating a database layer.
 * Kept in-memory intentionally so the demo project has zero external
 * dependencies (no real DB/driver required) while still modelling a
 * realistic layered architecture (model -> repository -> service -> web).
 */
public class EmployeeRepository {

    private final Map<Integer, Employee> store = new LinkedHashMap<>();
    private int nextId = 1;

    public Employee save(Employee employee) {
        if (employee.getId() == 0) {
            employee.setId(nextId++);
        }
        store.put(employee.getId(), employee);
        return employee;
    }

    public Optional<Employee> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Employee> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean existsByEmail(String email) {
        return store.values().stream()
                .anyMatch(e -> e.getEmail().equalsIgnoreCase(email));
    }

    public boolean deleteById(int id) {
        return store.remove(id) != null;
    }

    public void clear() {
        store.clear();
        nextId = 1;
    }
}
