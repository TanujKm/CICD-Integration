# PDLC & CI/CD Quality Integration — Demo Overview

---

## What is This Project

A simple Employee Management app built in Java.
The app is intentionally simple — the real point is to demonstrate
how PDLC quality integration works on top of any project.

App features:
- Add a new employee (name, email, department, salary)
- View all employees in a table
- Server-side validation (invalid email, zero salary, duplicate email rejected)

---

## Project Structure

```
java-demo-v2/
│
├── src/main/java/com/pdlc/demo/
│   ├── model/Employee.java              - employee data model
│   ├── util/ValidationUtil.java         - email, name, salary validation
│   ├── repository/EmployeeRepository.java - in-memory data store
│   ├── service/EmployeeService.java     - business logic
│   └── web/WebServer.java               - HTTP server (no framework)
│
├── src/main/resources/static/
│   ├── index.html                       - employee list page
│   └── add-employee.html                - add employee form
│
├── src/test/java/com/pdlc/demo/
│   ├── model/EmployeeTest.java          - unit test
│   ├── util/ValidationUtilTest.java     - unit test
│   ├── repository/EmployeeRepositoryTest.java - unit test
│   ├── service/EmployeeServiceTest.java - unit test
│   ├── service/EmployeeServiceBonusCalculationTest.java - unit test
│   ├── ui/EmployeeListPageSeleniumTest.java  - selenium e2e test
│   └── ui/AddEmployeePageSeleniumTest.java   - selenium e2e test
│
├── .github/workflows/quality-pipeline.yml  - CI/CD pipeline
├── sonar-project.properties                - SonarCloud config
├── dependency-check-suppressions.xml       - OWASP security config
├── .gitignore                              - excludes target/ from git
└── pom.xml                                 - Maven config (JaCoCo, Checkstyle)
```

---

## Test Layers — What is What

### Unit Tests (5 files)
- Located in: model, util, repository, service folders
- No browser, no server — directly test Java classes
- Fast — run in milliseconds
- How to identify: uses assertEquals, assertThrows directly on Java objects

### Selenium E2E Tests (2 files)
- Located in: ui folder
- Real browser opens (headless Chrome), real server starts
- Slow — run in seconds
- How to identify: uses ChromeDriver, driver.get(), driver.findElement()

### Integration Tests
- Not a separate Java file — written directly in pipeline YAML
- Server starts in background, curl hits API endpoints directly
- 3 checks: GET employees, POST new employee, POST duplicate email

---

## Pipeline — quality-pipeline.yml

What happens when code is pushed to GitHub:

```
Code Push / Pull Request
        |
        v
Stage 1 — Lint + Build
        Checkstyle (Google style) + mvn compile
        GATE: compile error = pipeline stops
        |
        v
Stage 2 — Unit Tests + Coverage
        5 unit test files run, JaCoCo coverage report generated
        GATE: any test fail = pipeline stops
        |
        v
Stage 2.5 — Integration Test (API)
        Server starts on port 8083
        curl hits GET /api/employees        → expects 200 + JSON array
        curl hits POST /api/employees       → expects 200 + success
        curl hits POST with duplicate email → expects 400 + error
        GATE: wrong response = pipeline stops
        |
        v
Stage 3 — Static Analysis          Stage 4 — Security Scan
(SonarCloud)                       (OWASP Dependency-Check)
Code smells, duplication,          Scans Maven dependencies
complexity check                   for known CVEs
GATE: quality gate fail            GATE: High/Critical CVE
= pipeline stops                   = pipeline stops
        |                                   |
        v-----------------------------------v
        |
        v
Stage 5 — Selenium E2E Regression
        Headless Chrome runs EmployeeListPageSeleniumTest
        and AddEmployeePageSeleniumTest
        GATE: any selenium test fail = pipeline stops
        |
        v
Stage 6 — Deploy to Staging  (main branch only)
        Builds JAR, deploys to staging server
        Runs smoke test after deploy
        Transitions Jira story to "Ready for UAT"
        |
        v
Stage 7 — Jira on Failure  (runs if anything above fails)
        Auto-creates a Jira bug with pipeline link,
        branch name, commit SHA, triggered by
```

---

## Config Files — What They Do

### sonar-project.properties
Tells SonarCloud where to find source code, test code, and coverage report.
Nothing complex — just paths pointing to src/main/java and jacoco.xml.

### dependency-check-suppressions.xml
Used by OWASP security scan. If a CVE is a false positive or accepted risk,
add it here with a reason. Currently empty — no suppressions needed yet.

### .gitignore
Keeps the target/ folder out of Git. Compiled classes and JARs
should never be committed to the repo.

### pom.xml
Maven config. Key plugins added:
- jacoco-maven-plugin — generates coverage report after mvn test
- maven-checkstyle-plugin — checks Google Java style
- maven-surefire-plugin — runs JUnit tests

---

## What is Working Right Now

| Thing | Status |
|---|---|
| Lint + Build | Working |
| Unit Tests (5 files) | Working |
| Integration Test (curl API) | Working |
| Selenium E2E (2 files) | Working (needs Chrome) |
| JaCoCo Coverage Report | Working |
| Pipeline YAML | Ready — triggers on push |
| SonarCloud | Wired — needs SONAR_TOKEN secret |
| OWASP Security Scan | Wired — soft gate |
| Jira Auto Bug | Wired — needs JIRA_API_TOKEN secret |

---

## What is Not Done Yet and Why

| Thing | Why Not Done | Where to Add When Ready |
|---|---|---|
| Real Deploy | No staging server available | deploy-staging job → replace echo with ./deploy.sh |
| Smoke Test | No live URL to hit | deploy-staging job → curl -f https://staging-url/api/employees |
| Rollback | No server to rollback on | deploy-staging job → if: failure() → ./rollback.sh |
| SonarCloud Gate | Need SONAR_TOKEN from sonarcloud.io | GitHub → Settings → Secrets → SONAR_TOKEN |
| Jira Integration | Need Jira API token | GitHub → Settings → Secrets → JIRA_API_TOKEN |
| Coverage at 80% | Progressive rollout strategy | pom.xml → jacoco minimum change 0.10 to 0.80 |

---

## How to Run Locally

### Run the app:
```
mvn compile exec:java "-Dexec.mainClass=com.pdlc.demo.web.WebServer"
```
Open browser: http://localhost:8080

### Run unit tests only (fast, no browser needed):
```
mvn test -Dtest="!*SeleniumTest"
```

### Run all tests including Selenium (Chrome needed):
```
mvn verify
```

### View coverage report:
After running tests, open this file in browser:
```
target/site/jacoco/index.html
```

---

## How Anyone Can Use This Pattern on Their Own Project

1. Copy .github/workflows/quality-pipeline.yml into your project
2. Change the run: commands to match your project (mvn, gradle, npm etc.)
3. Add sonar-project.properties with your project key
4. Add dependency-check-suppressions.xml as-is
5. Add SONAR_TOKEN and JIRA_API_TOKEN in GitHub → Settings → Secrets
6. Push — pipeline triggers automatically

That's it. The pattern works for any Java/Maven project.

---

## One Line Summary

> This project shows how to implement PDLC quality integration on a real
> Java project — automated quality gates from lint to Selenium E2E,
> all triggered automatically on every commit via GitHub Actions.



/////////////////////////////////////

1. Java App Banaya
Simple Employee Management app — add employee, list dekho. Pure Java, no framework, no database.

2. 7 Test Files Likhe
5 unit test files — model, util, repository, service, bonus
2 Selenium E2E files — list page, add employee form
3. CI/CD Pipeline Banaya
quality-pipeline.yml
 — 7 stages:

Lint → Unit Test → Integration Test → SonarCloud → OWASP → Selenium → Deploy
4. Config Files Add Kiye
sonar-project.properties — SonarCloud ke liye
dependency-check-suppressions.xml — OWASP ke liye
.gitignore — target/ folder exclude
pom.xml updated — JaCoCo + Checkstyle add kiye
5. Demo Overview File Banaya
DEMO-OVERVIEW.md — sab kuch ek jagah, demo ke liye ready

6. Code Human-Like Banaya
Pipeline file se over-documentation, emoji, fancy headers — sab hata diye. Normal developer jaisa dikhta hai ab.




/////////////////




Maine Java + Selenium choose kiya kyunki:

Yeh meri existing skill set hai
Automation testing ka background h



Tools & Technologies — With Versions


Application

Tool	Version
Java	17
Maven	3.9+
JDK Built-in HTTP Server	Java 17 (com.sun.net.httpserver)



Testing
Tool	Version
JUnit Jupiter	5.10.2
Selenium Java	4.31.0
Chrome Browser	Stable (headless)




Code Quality
Tool	Version
JaCoCo	0.8.12
Maven Checkstyle Plugin	3.3.1
Google Checks (Checkstyle ruleset)	built-in



Security
Tool	Version
OWASP Dependency-Check Maven	9.2.0


Static Analysis
Tool	Version
SonarCloud	Latest (SaaS)
sonarqube-scan-action	v5



CI/CD & Pipeline



Tool	Version

GitHub Actions	Latest
actions/checkout	v4
actions/setup-java	v4
actions/upload-artifact	v4
browser-actions/setup-chrome	v1




Project Management / Traceability
Tool	Version
Jira	Cloud (SaaS)
atlassian/gajira-create	v3
atlassian/gajira-transition	v3



Build & Plugins
Tool	Version
maven-surefire-plugin	3.2.5
exec-maven-plugin