package nestnz.org;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class LoginPageTestSuites {
	  private WebDriver driver;
	  private String baseUrl;
	  private boolean acceptNextAlert = true;
	  private StringBuffer verificationErrors = new StringBuffer();

	  @Before
	  public void setUp() throws Exception {
	    baseUrl = "https://www.nestnz.org/";
	  }

	 /* @Test
	  public void LaunchTestOnIE() throws Exception {
		System.setProperty("webdriver.edge.driver",".\\Browsers\\MicrosoftWebDriver.exe");
		driver = new EdgeDriver();
		  driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		  testLoginPageTestSuites();
	  }*/


	  @Test
	  public void LaunchTestOnFireFox() throws Exception {
		  System.setProperty("webdriver.gecko.driver",".\\Browsers\\geckodriver.exe");
	      driver = new FirefoxDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testLoginPageTestSuites();
	  }
	  @Test
	  public void LaunchTestOnChrome() throws Exception {
		  System.setProperty("webdriver.chrome.driver", ".\\Browsers\\chromedriver.exe");
	      driver = new ChromeDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testLoginPageTestSuites();
	  }

  private void testLoginPageTestSuites() throws Exception {
    driver.get("https://www.nestnz.org");
    driver.findElement(By.linkText("Log In")).click();
    driver.findElement(By.id("username")).clear();
    driver.findElement(By.id("username")).sendKeys("mjdev");
    driver.findElement(By.id("password")).clear();
    driver.findElement(By.id("password")).sendKeys("mjdevadmin");
    assertTrue(isElementPresent(By.xpath("//body/div/div")));
    assertTrue(isElementPresent(By.xpath("//div/div/div")));
    assertEquals("Admin Log In", driver.findElement(By.cssSelector("h3.text-center.login-title")).getText());
    assertEquals("Email", driver.findElement(By.cssSelector("label")).getText());
    assertTrue(isElementPresent(By.cssSelector("i.fa.fa-key")));
    assertEquals("Password", driver.findElement(By.xpath("//div[2]/label")).getText());
    assertTrue(isElementPresent(By.cssSelector("i.fa.fa-lock")));
    assertTrue(isElementPresent(By.xpath("//button[@type='submit']")));
    assertEquals("Log in", driver.findElement(By.xpath("//button[@type='submit']")).getText());
    driver.findElement(By.id("username")).clear();
    driver.findElement(By.id("username")).sendKeys("");
    driver.findElement(By.id("password")).clear();
    driver.findElement(By.id("password")).sendKeys("");
    assertTrue(isElementPresent(By.id("username")));
    assertTrue(isElementPresent(By.id("password")));
  }

  @After
  public void tearDown() throws Exception {
    driver.quit();
    String verificationErrorString = verificationErrors.toString();
    if (!"".equals(verificationErrorString)) {
      fail(verificationErrorString);
    }
  }

  private boolean isElementPresent(By by) {
    try {
      driver.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  private boolean isAlertPresent() {
    try {
      driver.switchTo().alert();
      return true;
    } catch (NoAlertPresentException e) {
      return false;
    }
  }

  private String closeAlertAndGetItsText() {
    try {
      Alert alert = driver.switchTo().alert();
      String alertText = alert.getText();
      if (acceptNextAlert) {
        alert.accept();
      } else {
        alert.dismiss();
      }
      return alertText;
    } finally {
      acceptNextAlert = true;
    }
  }
}
