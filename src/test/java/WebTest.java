import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WebTest {
    static String[] filenames = { "data1.txt", "data2.txt" };

    static record UserCredentials (
            String email,
            String newEmail,
            String password,
            String firstName,
            String lastName,
            char gender
    ) {};

    private static UserCredentials credentials;

    private static Stream<Arguments> provideData() {
        var dataLists = Arrays.stream(filenames).toList().stream().map(WebTest::getData).toList();
        var argsList = dataLists.stream().map(dataList -> Arguments.of(credentials, dataList));
        return Stream.of(
                argsList.toArray(Arguments[]::new)
        );
    }

    private static Stream<Arguments> provideCredentials() {
        return Stream.of(
                Arguments.of(credentials)
        );
    }

    public static ArrayList<String> getData(String filename) {
        try {
            ArrayList<String> data = new ArrayList<>();
            data.ensureCapacity(5);
            try (BufferedReader fileReader = new BufferedReader(new FileReader(ClassLoader.getSystemResource(filename).getFile()))) {
                String line;
                while (null != (line = fileReader.readLine())) {
                    data.add(line);
                }
                return data;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void login(WebDriverWait wait, String email, String password) {
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text() = 'Log in']")));
        loginLink.click();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("Email"))).sendKeys(email);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("Password"))).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@value = 'Log in']"))).click();
    }

    public static void logout(WebDriverWait wait) {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text() = 'Log out']")))
                .click();
    }

    @BeforeAll
    public static void registerUser() {
        // Arrange
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        driver.get("https://demowebshop.tricentis.com");

        credentials = new UserCredentials(
                Randomizer.email(),
                Randomizer.email(),
                Randomizer.password(),
                Randomizer.name(),
                Randomizer.name(),
                Randomizer.gender()
                );

        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text() = 'Log in']")));
        loginLink.click();

        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@value = 'Register']")));
        registerButton.click();

        WebElement randomGenderRadioButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                String.format("//input[@name='Gender' and @value = '%c']", credentials.gender)
        )));
        randomGenderRadioButton.click();

        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("FirstName")));
        firstNameField.sendKeys(credentials.firstName);

        WebElement lastNameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("LastName")));
        lastNameField.sendKeys(credentials.lastName);

        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("Email")));
        emailField.sendKeys(credentials.email);

        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.id("Password")));
        passwordField.sendKeys(credentials.password);
        WebElement confirmPasswordField = wait.until(ExpectedConditions.elementToBeClickable(By.id("ConfirmPassword")));
        confirmPasswordField.sendKeys(credentials.password);

        WebElement registerSubmitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@value = 'Register']")));
        registerSubmitButton.click();

        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@value = 'Continue']")));
        continueButton.click();


        driver.quit();
    }

    @ParameterizedTest
    @Order(1)
    @MethodSource("provideData")
    public void orderEndToEnd(UserCredentials credentials, ArrayList<String> digitalDownloads) {
        // Arrange

        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        // 1
        driver.get("https://demowebshop.tricentis.com");

        // 2
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text() = 'Log in']")));
        loginLink.click();

        // 3
        login(wait, credentials.email(), credentials.password());

        // 4
        WebElement digitalDownloadsLink = wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath("//a[@href = '/digital-downloads']"))));
        digitalDownloadsLink.click();

        // 5
        for (String digitalDownload : digitalDownloads) {
            WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    String.format("//div[@class = 'details' and descendant::a[text() = '%s']]" +
                            "/descendant::input[@value = 'Add to cart']", digitalDownload)
            )));
            addToCartButton.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'ajax-loading-block-window')]")));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[contains(@class, 'ajax-loading-block-window')]")));
        }

        // 6
        WebElement shoppingCartLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href = '/cart']")));
        shoppingCartLink.click();

        // 7
        wait.until(ExpectedConditions.elementToBeClickable(By.id("termsofservice"))).click(); // I agree
        wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout"))).click(); // Checkout

        // 8
        Select select = new Select(driver.findElement(By.xpath(
                "//select[@id = 'BillingNewAddress_CountryId' or @id = 'billing-address-select']"
        )));
        List<WebElement> options = select.getOptions();
        Assertions.assertFalse(options.isEmpty());

        String id = select.getWrappedElement().getDomAttribute("id");
        Assertions.assertNotNull(id);

        if (id.equals("BillingNewAddress_CountryId")) {
            int randomIndex = new Random().nextInt(options.size() - 1) + 1;
            select.selectByIndex(randomIndex);
            Map<String, String> fields = Map.of(
                    "BillingNewAddress_City", Randomizer.name(),
                    "BillingNewAddress_Address1", Randomizer.name(),
                    "BillingNewAddress_ZipPostalCode", Randomizer.zipCode(),
                    "BillingNewAddress_PhoneNumber", Randomizer.phone()
            );
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(entry.getKey())));
                element.clear();
                element.sendKeys(entry.getValue());
            }
        } else {
            select.selectByContainsVisibleText(credentials.firstName);
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@value = 'Continue']")))
                .click();

        // 9 - Payment Method
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//div[@id = 'payment-method-buttons-container']/descendant::input[@value = 'Continue']"
        ))).click();

        // 10 - Payment Information
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//div[@id = 'payment-info-buttons-container']/descendant::input[@value = 'Continue']"
        ))).click();

        // 11
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@value = 'Confirm']")))
                .click();

        // 12
        var a = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//div[contains(@class, 'order-completed')]" +
                        "/child::div[@class = 'title']" +
                        "/strong"
        )));

        Assertions.assertEquals("Your order has been successfully processed!", a.getText());

        driver.quit();
    }

    @ParameterizedTest
    @Order(999)
    @MethodSource("provideCredentials")
    public void changeEmail(UserCredentials credentials) {
        // Arrange

        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        driver.get("https://demowebshop.tricentis.com");

        login(wait, credentials.email(), credentials.password());

        WebElement customerInfoLink = wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath("//a[@href = '/customer/info']"))));
        customerInfoLink.click();

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id = 'Email']")));
        emailInput.clear();
        emailInput.sendKeys(credentials.newEmail());

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@value = 'Save']")))
                .click();

        logout(wait);
        login(wait, credentials.newEmail(), credentials.password());

        logout(wait);
        login(wait, credentials.email(), credentials.password());

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[contains(text(), 'No customer account found')]")));

        driver.quit();
    }

}
