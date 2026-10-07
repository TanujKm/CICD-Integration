# PDLC & CI/CD — Employee Management Demo Project
### (Java + Maven + Selenium — Tester Skillset)

A structured, layered Java project — built to look and feel like a real
company codebase — created to demonstrate the **PDLC & CI/CD Quality
Integration Model** (Confluence page) using the tester's own skillset
(Java, Selenium, JUnit). **No CI/CD pipeline is pre-integrated** — that
part is added by the tester, as instructed.

## Architecture

```
model        -> Employee.java                (domain object)
util         -> ValidationUtil.java           (input validation rules)
repository   -> EmployeeRepository.java       (in-memory data layer)
service      -> EmployeeService.java          (business logic)
web          -> WebServer.java                (HTTP server + REST-style API)
resources    -> index.html, add-employee.html (UI pages)
```

This is a standard **layered architecture** (model → repository → service
→ web), the same pattern used in most real Java applications — kept
dependency-light (JDK's built-in HTTP server, no framework) so it builds
without needing network access to pull in a full framework.

## Features

- List all employees (`GET /api/employees`, rendered on `index.html`)
- Add a new employee with server-side validation
  (`POST /api/employees`, form on `add-employee.html`)
- Business rules: duplicate-email check, salary must be positive,
  annual bonus calculation (0–50% of salary)

## Test suite — 7 test files

| # | File | Layer tested | Maps to doc section |
|---|---|---|---|
| 1 | `model/EmployeeTest.java` | Model | Section 4 — Development |
| 2 | `util/ValidationUtilTest.java` | Validation rules | Section 4 — Development |
| 3 | `repository/EmployeeRepositoryTest.java` | Data layer | Section 5 — Unit Test Gate |
| 4 | `service/EmployeeServiceTest.java` | Business logic (CRUD + validation) | Section 5 — Unit Test Gate |
| 5 | `service/EmployeeServiceBonusCalculationTest.java` | Business rule (bonus calc) | Section 5 — Unit Test Gate |
| 6 | `ui/EmployeeListPageSeleniumTest.java` | UI — list page | Section 5 — Automated Regression (E2E) |
| 7 | `ui/AddEmployeePageSeleniumTest.java` | UI — add form, full flow | Section 5 — Automated Regression (E2E) |

Tests 1–5 use JUnit only (fast, no browser needed).
Tests 6–7 use Selenium against a real running instance of the app —
this is the part that directly showcases the tester's own skillset.

## Prerequisites to run locally

- Java 17+
- Maven 3.9+
- Google Chrome + matching `chromedriver` on PATH (for tests 6–7)

## How to run

```bash
# Run only fast unit tests (1-5), skip Selenium
mvn test -Dtest='!*SeleniumTest'

# Run everything including Selenium E2E tests (needs Chrome)
mvn verify

# Coverage report:
target/site/jacoco/index.html
```

To browse the app manually:
```bash
mvn compile exec:java -Dexec.mainClass="com.pdlc.demo.web.WebServer"
# open http://localhost:8080
```

## What to build next (the actual PoC task)

1. Push to a **new, empty GitHub repository** (never a live/production repo).
2. Add `.github/workflows/quality-pipeline.yml`:
   - Checkout → Setup JDK 17 → `mvn checkstyle:check` (lint)
   - `mvn test -Dtest='!*SeleniumTest'` (unit test + coverage gate)
   - `browser-actions/setup-chrome` → `mvn verify` (adds Selenium regression gate)
3. Add SonarCloud (`sonar-maven-plugin`) once account/token is available.
4. Add Jira auto bug-creation/status-transition once Jira access is available..
5. **Demo flow for the lead:**
   - Show the pipeline passing end-to-end.
   - Break `EmployeeServiceTest.java` on purpose → show it failing.
   - Fix it → show it passing.
   - Break `AddEmployeePageSeleniumTest.java` (e.g. change expected text)
     → show the E2E gate catching it too — this is the moment that
     showcases the Selenium skillset specifically..

## Why this project

- Layered, realistic structure (not a single-file script) — looks and
  reads like an actual small company application.
- 7 test files spanning unit, business-rule, and E2E/Selenium layers —
  covers every testing concern named in Section 5 of the document.
- Fully self-built and explainable — no unfamiliar third-party code.
- Zero cost, zero licensing concerns, no pre-existing CI/CD to strip out.



## Status
Pipeline configured with SonarCloud integration.
