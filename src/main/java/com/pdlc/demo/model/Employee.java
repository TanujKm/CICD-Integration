package com.pdlc.demo.model;

import java.util.Objects;

/**
 * Domain model representing an Employee.
 */
public class Employee {

    private int id;
    private String name;
    private String email;
    private String department;
    private double salary;

    public Employee(int id, String name, String email, String department, double salary) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return id == employee.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", name='" + name + "', email='" + email
                + "', department='" + department + "', salary=" + salary + "}";
    }
}
