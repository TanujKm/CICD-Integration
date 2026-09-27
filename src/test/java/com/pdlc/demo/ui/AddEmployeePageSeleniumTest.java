package com.pdlc.demo.ui;

import com.pdlc.demo.repository.EmployeeRepository;
import com.pdlc.demo.service.EmployeeService;
import com.pdlc.demo.util.ValidationUtil;
import com.pdlc.demo.web.WebServer;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Selenium E2E test — Add Employee form.
 * Exercises the full flow: fill form -> submit -> API call -> success
 * message rendered on the page. Maps to the "Automated Regression (E2E)"
 * gate in Section 5 of the PDLC & CI/CD Quality Integration Model.
 */
class AddEmployeePageSeleniumTest {

    private static WebServer server;
    private WebDriver driver;

    @BeforeAll
    static void startServer() throws IOException {
        EmployeeRepository repository = new EmployeeRepository();
        EmployeeService service = new EmployeeService(repository, new ValidationUtil());
        server = new WebServer(8082, service);
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setUpDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testAddingNewEmployeeShowsSuccessMessage() {
        driver.get("http://localhost:8082/add-employee.html");

        driver.findElement(By.id("name")).sendKeys("New Tester");
        driver.findElement(By.id("email")).sendKeys("new.tester@example.com");
        driver.findElement(By.id("department")).sendKeys("QA");
        driver.findElement(By.id("salary")).sendKeys("48000");
        driver.findElement(By.id("submitBtn")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        boolean status = wait.until(
                ExpectedConditions.textToBePresentInElementLocated(By.id("statusMessage"), "success")
        );

        assertTrue(status);
    }

    @Test
    void testAddingEmployeeWithInvalidEmailShowsError() {
        driver.get("http://localhost:8082/add-employee.html");

        driver.findElement(By.id("name")).sendKeys("Bad Email Tester");
        // Using type=email input, an invalid format like this is still submittable via JS-driven fetch
        driver.findElement(By.id("email")).sendKeys("bad-email-format");
        driver.findElement(By.id("department")).sendKeys("QA");
        driver.findElement(By.id("salary")).sendKeys("48000");
        driver.findElement(By.id("submitBtn")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        boolean status = wait.until(
                ExpectedConditions.textToBePresentInElementLocated(By.id("statusMessage"), "Error")
        );

        assertTrue(status);
    }
}
