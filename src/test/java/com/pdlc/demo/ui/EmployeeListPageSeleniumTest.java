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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeListPageSeleniumTest {

    private static WebServer server;
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    static void startServer() throws IOException {
        EmployeeRepository repository = new EmployeeRepository();
        EmployeeService service = new EmployeeService(repository, new ValidationUtil());
        service.addEmployee("Aditi Sharma", "aditi@example.com", "QA", 50000);
        service.addEmployee("Rohan Mehta", "rohan@example.com", "Engineering", 60000);

        server = new WebServer(8081, service);
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
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testEmployeeListPageShowsSeededEmployees() {
        driver.get("http://localhost:8081/");

        // wait until at least 2 rows are present — JS fetch may take a moment
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.className("employee-row"), 1));

        List<WebElement> rows = driver.findElements(By.className("employee-row"));

        assertEquals(2, rows.size());
    }

    @Test
    void testEmployeeListPageHasCorrectHeading() {
        driver.get("http://localhost:8081/");

        WebElement heading = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));

        assertTrue(heading.getText().contains("Employee List"));
    }
}
