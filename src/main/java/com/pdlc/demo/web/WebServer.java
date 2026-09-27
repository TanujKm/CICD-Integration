package com.pdlc.demo.web;

import com.pdlc.demo.model.Employee;
import com.pdlc.demo.repository.EmployeeRepository;
import com.pdlc.demo.service.EmployeeService;
import com.pdlc.demo.util.ValidationUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight HTTP server (JDK built-in com.sun.net.httpserver — no
 * external web framework dependency) exposing:
 *   GET  /                    -> employee list page
 *   GET  /add-employee.html   -> add employee form page
 *   GET  /api/employees       -> JSON list of employees
 *   POST /api/employees       -> add a new employee (form-encoded body)
 *
 * Kept dependency-free intentionally so the project builds without
 * network access to a Maven repository for a full framework.
 */
public class WebServer {

    private final HttpServer server;
    private final EmployeeService employeeService;

    public WebServer(int port, EmployeeService employeeService) throws IOException {
        this.employeeService = employeeService;
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/employees", this::handleEmployeesApi);
        server.createContext("/", this::handleStaticFiles);
    }

    private void handleEmployeesApi(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (method.equalsIgnoreCase("GET")) {
            List<Employee> employees = employeeService.getAllEmployeesSortedByName();
            String json = toJsonArray(employees);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        } else if (method.equalsIgnoreCase("POST")) {
            Map<String, String> params = parseFormBody(exchange);
            try {
                Employee saved = employeeService.addEmployee(
                        params.get("name"),
                        params.get("email"),
                        params.get("department"),
                        Double.parseDouble(params.getOrDefault("salary", "0"))
                );
                String response = "{\"status\":\"success\",\"id\":" + saved.getId() + "}";
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            } catch (IllegalArgumentException ex) {
                String response = "{\"status\":\"error\",\"message\":\"" + ex.getMessage() + "\"}";
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, bytes.length);
                exchange.getResponseBody().write(bytes);
            } finally {
                exchange.getResponseBody().close();
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }
        String resourcePath = "static" + path;

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                String notFound = "404 Not Found";
                exchange.sendResponseHeaders(404, notFound.length());
                exchange.getResponseBody().write(notFound.getBytes());
            } else {
                byte[] bytes = is.readAllBytes();
                exchange.getResponseHeaders().add("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            }
        } finally {
            exchange.getResponseBody().close();
        }
    }

    private Map<String, String> parseFormBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(
                        URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                );
            }
        }
        return params;
    }

    private String toJsonArray(List<Employee> employees) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);
            sb.append("{\"id\":").append(e.getId())
              .append(",\"name\":\"").append(e.getName()).append("\"")
              .append(",\"email\":\"").append(e.getEmail()).append("\"")
              .append(",\"department\":\"").append(e.getDepartment()).append("\"")
              .append(",\"salary\":").append(e.getSalary())
              .append("}");
            if (i < employees.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public static void main(String[] args) throws IOException {
        EmployeeRepository repository = new EmployeeRepository();
        ValidationUtil validationUtil = new ValidationUtil();
        EmployeeService service = new EmployeeService(repository, validationUtil);

        // Seed a couple of sample employees so the list page isn't empty on first run
        service.addEmployee("Aditi Sharma", "aditi.sharma@example.com", "QA", 55000);
        service.addEmployee("Rohan Mehta", "rohan.mehta@example.com", "Engineering", 72000);

        WebServer webServer = new WebServer(8080, service);
        webServer.start();
        System.out.println("Employee Management demo running at http://localhost:8080");
    }
}
