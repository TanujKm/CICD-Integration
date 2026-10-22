# PDLC & CI/CD Quality Integration — Demo Overview

---

## Why This Project

Our team wrote a Confluence page on "PDLC & CI/CD Quality Integration".
The task was to build a working demo that proves the concept — not just document it.

I chose Java + Selenium because that is my existing skillset as an automation tester.
The app is intentionally simple — the point is the quality integration on top of it, not the app itself.

---

## App Features

A simple Employee Management web app:
- Add a new employee (name, email, department, salary)
- View all employees in a table
- Server-side validation — invalid email, zero salary, duplicate email all rejected
- Annual bonus calculation logic (0–50% of salary)
- Employees sorted alphabetically by name

Run the app:
```
mvn compile exec:java "-Dexec.mainClass=com.pdlc.demo.web.WebServer"
```
Then open: http://localhost:8080

---

## Project Structure

```
java-demo-v2/
│
├── src/main/java/com/pdlc/demo/
│   ├── model/Employee.java                           - employee data model
│   ├── util/ValidationUtil.java                      - email, name, salary validation rules
│   ├── repository/EmployeeRepository.java            - in-memory data store (no real DB)
│   ├── service/EmployeeService.java                  - business logic + bonus calculation
│   └── web/WebServer.java                            - lightweight HTTP server, no framework
│
├── src/main/resources/static/
│   ├── index.html                                    - employee list page
│   └── add-employee.html                             - add employee form
│
├── src/test/java/com/pdlc/demo/
│   ├── model/EmployeeTest.java                       - unit test
│   ├── util/ValidationUtilTest.java                  - unit test
│   ├── repository/EmployeeRepositoryTest.java        - unit test
│   ├── service/EmployeeServiceTest.java              - unit test
│   ├── service/EmployeeServiceBonusCalculationTest   - unit test (business rule)
│   ├── ui/EmployeeListPageSeleniumTest.java          - selenium e2e test
│   └── ui/AddEmployeePageSeleniumTest.java           - selenium e2e test
│
├── .github/workflows/quality-pipeline.yml           - full CI/CD pipeline
├── dependency-check-suppressions.xml                - OWASP CVE suppressions
├── .gitignore                                        - excludes target/ from git
└── pom.xml                                           - Maven config
```

---

## Test Layers

### Unit Tests — 5 files
- Location: model, util, repository, service folders
- No browser, no server — directly call Java classes
- Fast — milliseconds
- Identify by: `assertEquals`, `assertThrows` on Java objects directly

### Selenium E2E Tests — 2 files
- Location: ui folder
- Real headless Chrome browser, real embedded server
- Slow — seconds
- Identify by: `ChromeDriver`, `driver.get()`, `driver.findElement()`

### Integration Tests — in pipeline YAML (not a Java file)
- Server starts on port 8080 in background
- curl hits 3 API endpoints directly
- GET /api/employees → 200 + JSON array
- POST /api/employees → 200 + success
- POST same email again → 400 + duplicate error message

---

## Pipeline — quality-pipeline.yml

Triggered automatically on every push or PR to main/master/IIntegration branches.

```
Code Push
    |
    v
extract-jira-key
    Reads branch name, PR title, commit message
    Extracts Jira key pattern like QA-1
    Used by Jira steps later
    |
    v
check-jira-key  (PR only)
    Warns if no Jira key found in branch name
    Enforces naming convention — soft warning, not a hard block
    |
    v
Stage 1 — Lint + Build
    Checkstyle (Google Java style) — soft gate
    mvn compile — hard gate, pipeline stops on compile error
    |
    v
Stage 2 — Unit Tests + Coverage
    5 unit test files run via JUnit
    JaCoCo generates coverage report (currently 10% threshold — soft)
    Artifacts uploaded: jacoco-report, unit-test-results
    |
    v
Stage 2.5 — Integration Test (API)
    Builds JAR, starts server on port 8080
    curl: GET /api/employees → 200 + JSON array
    curl: POST new employee → 200 + success
    curl: POST duplicate email → 400 + already exists
    Hard gate — wrong response = pipeline stops
    |
    v
Stage 3 — Static Analysis (SonarQube/SonarCloud)
    FUTURE SCOPE — pending company credentials
    Will check code smells, bugs, duplication, coverage
    |
    v
Stage 4 — Security Scan (OWASP Dependency-Check)
    Scans Maven dependencies for known CVEs
    Soft gate currently (continue-on-error: true)
    Needs NVD_API_KEY secret for reliable results
    |
    v
Stage 5 — Selenium E2E Regression
    Headless Chrome via browser-actions/setup-chrome
    EmployeeListPageSeleniumTest — checks list page loads with data
    AddEmployeePageSeleniumTest — checks form submit success and error cases
    Hard gate — any selenium failure = pipeline stops
    |
    v
Stage 6 — Deploy to Staging  (main/master push only)
    Builds deployable JAR
    Deploy step — stub currently (echo), replace with real command when server ready
    Smoke test — stub currently, replace with curl health check URL
    |
    v
jira-on-pass  (all branches, after e2e passes)
    Logs in to Jira
    Comments on linked Jira issue: "Pipeline passed, Build #N, run link"
    Transitions issue to "In Review" (or configured status)
    Searches for open bug with tracking label ci-QA-1
    If found: comments "Fixed in build #N - run link"
    |
    v
jira-on-failure  (runs only when pipeline fails)
    Checks if same tracking-label bug already exists (JQL search)
    If yes: comments "Failed again, run #N" on existing bug
    If no: creates new Bug in Jira project with:
        - title: [CI Failure] QA-1: pipeline failed (#N, abc1234)
        - labels: ci-failure, automated, ci-QA-1
        - description: failed jobs, branch, commit, author, run link
        - Relates link to original Jira issue (via REST API)
```

---

## Jira Integration — How It Works

### Key Extraction
Pipeline reads branch name → PR title → commit message in that order.
First match of pattern `[A-Z]+-[0-9]+` (like QA-1) is used.
If no key found: Jira steps still run but without parent link.

### On Pipeline Pass
- Comment added to linked Jira issue with build number and run URL
- Issue status transitions automatically
- If there was a previous open bug for this branch: "Fixed in build #N" comment added

### On Pipeline Fail
- Duplicate check first — JQL searches for open bug with label `ci-QA-1`
- If open bug exists: "Failed again, run #N" comment added (no duplicate bug)
- If no open bug: new Bug created with full details and linked via Relates

### Naming Convention
Branch or commit message should include Jira key:
```
feature/QA-1-add-validation
git commit -m "QA-1 fix login validation"
```

---

## Config Files

### dependency-check-suppressions.xml
Used by OWASP scan. Add entries here for false positives with a reason and date.
Currently empty — no suppressions needed yet.

### .gitignore
Excludes target/ (compiled classes, JARs) from Git.

### pom.xml — Key Plugins
- maven-jar-plugin — makes JAR executable (java -jar)
- jacoco-maven-plugin — coverage report after mvn test
- maven-checkstyle-plugin — Google Java style check
- maven-surefire-plugin — runs JUnit tests

---

## What is Fully Working

| Thing | Status |
|---|---|
| App runs locally | Working |
| Unit Tests (5 files) | Working |
| Integration Test (curl API) | Working |
| Selenium E2E (2 files) | Working |
| JaCoCo Coverage Report | Working — uploaded as artifact |
| Pipeline triggers on push/PR | Working |
| Jira key extraction | Working |
| Jira comment on pass | Working |
| Jira status transition on pass | Working |
| Jira duplicate bug check | Working |
| Jira bug create on failure | Working |
| Jira "Fixed in build" comment | Working |
| OWASP Security Scan | Working (soft gate, report when NVD key added) |

---

## What is Pending (Infrastructure Dependent)

| Thing | Reason | How to Activate |
|---|---|---|
| Real Staging Deploy | No staging server | Replace echo in deploy-staging job with real command |
| Smoke Test | No live URL | Replace echo with: curl -f https://staging-url/api/employees |
| Rollback | No server | Add: if: failure() → ./rollback.sh in deploy-staging |
| SonarQube/SonarCloud | Company credentials pending | Uncomment Stage 3 in pipeline, add SONAR_TOKEN secret |
| OWASP full scan | NVD API key pending | Add NVD_API_KEY in GitHub Secrets |
| Coverage at 80% | Progressive rollout strategy | pom.xml → jacoco minimum: change 0.10 to 0.80 |

---

## How to Run Tests

Unit tests only (fast, no browser):
```
mvn test -Dtest="!*SeleniumTest"
```

All tests including Selenium (Chrome needed):
```
mvn verify
```

Coverage report — open in browser after running tests:
```
target/site/jacoco/index.html
```

---

## Tools and Versions

| Category | Tool | Version |
|---|---|---|
| Language | Java | 17 |
| Build | Maven | 3.9+ |
| Testing | JUnit Jupiter | 5.10.2 |
| Testing | Selenium Java | 4.31.0 |
| Coverage | JaCoCo | 0.8.12 |
| Style | Maven Checkstyle Plugin | 3.3.1 |
| Security | OWASP Dependency-Check | 9.2.0 |
| CI/CD | GitHub Actions | Latest |
| CI/CD | actions/checkout | v4 |
| CI/CD | actions/setup-java | v4 |
| CI/CD | browser-actions/setup-chrome | v1 |
| Traceability | Jira Cloud | SaaS |
| Traceability | atlassian/gajira-login | v3 |
| Traceability | atlassian/gajira-create | v3 |
| Traceability | atlassian/gajira-comment | v3 |
| Traceability | atlassian/gajira-transition | v3 |
| Static Analysis | SonarQube/SonarCloud | Future scope |

---

## How to Reuse This Pattern on Any Java Project

1. Copy `.github/workflows/quality-pipeline.yml` into your project
2. Update the `run:` commands to match your build tool (mvn, gradle, npm)
3. Add `dependency-check-suppressions.xml` as-is
4. Set GitHub Secrets: `JIRA_API_TOKEN`, `JIRA_BASE_URL`, `JIRA_USER_EMAIL`
5. Set GitHub Variables: `JIRA_PROJECT_KEY`
6. Name your branches with Jira key: `feature/QA-123-description`
7. Push — pipeline triggers automatically, Jira updates automatically

---

## Demo Flow for Team

1. Show pipeline green run in GitHub Actions
2. Open Jira — show comment on QA-1 task with build link
3. Break a unit test intentionally → push → show pipeline fail
4. Show Jira — new Bug auto-created with ci-failure label, linked to QA-1
5. Fix the test → push → show pipeline green again
6. Show Jira — "Fixed in build #N" comment on the bug
7. Point to Stage 2.5 logs — show curl API test output (GET 200, POST 200, duplicate 400)
8. Point to Stage 5 logs — show Selenium headless Chrome test results

---

## Pilot Activity — M1 Milestone Value

This is what the lead asked to demonstrate as the core value of this integration.

Once the pipeline is stable (which it is now), the only ongoing work for QA is:

> Add or update test cases. Pipeline picks them up automatically. No pipeline changes needed.

How it works in practice:

- New feature gets added to the app
- QA writes a new unit test or Selenium test in the relevant folder
- Push the code
- Pipeline automatically runs the new test in the correct stage
- If it passes — Jira issue gets a comment and status update
- If it fails — Jira bug auto-created with full details

This means the team does not need to manually trigger tests, manually update Jira,
or manually check coverage — it all happens on every single commit.



That is the M1 value: shift quality left, automate the feedback loop,
and reduce manual QA effort on every release cycle.

