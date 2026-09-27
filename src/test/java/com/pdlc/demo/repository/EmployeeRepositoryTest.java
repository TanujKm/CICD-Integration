package com.pdlc.demo.repository;

import com.pdlc.demo.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeRepositoryTest {

    private EmployeeRepository repository;

    @BeforeEach
    void setUp() {
        repository = new EmployeeRepository();
    }

    @Test
    void testSaveAssignsAutoIncrementedId() {
        Employee e1 = repository.save(new Employee(0, "Aditi Sharma", "aditi@example.com", "QA", 50000));
        Employee e2 = repository.save(new Employee(0, "Rohan Mehta", "rohan@example.com", "Engineering", 60000));

        assertEquals(1, e1.getId());
        assertEquals(2, e2.getId());
    }

    @Test
    void testFindByIdReturnsCorrectEmployee() {
        Employee saved = repository.save(new Employee(0, "Aditi Sharma", "aditi@example.com", "QA", 50000));

        Optional<Employee> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Aditi Sharma", found.get().getName());
    }

    @Test
    void testFindByIdReturnsEmptyForUnknownId() {
        Optional<Employee> found = repository.findById(999);
        assertTrue(found.isEmpty());
    }

    @Test
    void testFindAllReturnsAllSavedEmployees() {
        repository.save(new Employee(0, "Aditi Sharma", "aditi@example.com", "QA", 50000));
        repository.save(new Employee(0, "Rohan Mehta", "rohan@example.com", "Engineering", 60000));

        List<Employee> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testExistsByEmailIsCaseInsensitive() {
        repository.save(new Employee(0, "Aditi Sharma", "Aditi@Example.com", "QA", 50000));

        assertTrue(repository.existsByEmail("aditi@example.com"));
    }

    @Test
    void testDeleteByIdRemovesEmployee() {
        Employee saved = repository.save(new Employee(0, "Aditi Sharma", "aditi@example.com", "QA", 50000));

        boolean deleted = repository.deleteById(saved.getId());

        assertTrue(deleted);
        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
