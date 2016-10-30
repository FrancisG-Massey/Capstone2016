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

public class EditUserPageTestSuites {
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
		  testEditUserPageTestSuites();
	  }*/

	  @Test
	  public void LaunchTestOnFireFox() throws Exception {
		  System.setProperty("webdriver.gecko.driver",".\\Browsers\\geckodriver.exe");
	      driver = new FirefoxDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testEditUserPageTestSuites();
	  }
	  @Test
	  public void LaunchTestOnChrome() throws Exception {
		  System.setProperty("webdriver.chrome.driver", ".\\Browsers\\chromedriver.exe");
	      driver = new ChromeDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testEditUserPageTestSuites();
	  }
	  
  private void testEditUserPageTestSuites() throws Exception {
    driver.get(baseUrl + "/#/");
    driver.findElement(By.linkText("Log In")).click();
    driver.findElement(By.id("username")).clear();
    driver.findElement(By.id("username")).sendKeys("mjdev");
    driver.findElement(By.id("password")).clear();
    driver.findElement(By.id("password")).sendKeys("mjdevadmin");
    driver.findElement(By.id("username")).clear();
    driver.findElement(By.id("username")).sendKeys("mj@nestnz.org");
    driver.findElement(By.xpath("//button[@type='submit']")).click();
    driver.findElement(By.linkText("Admin")).click();
    driver.findElement(By.linkText("Users")).click();
    driver.findElement(By.linkText("Edit")).click();
    assertEquals("Edit admin@nestnz.org", driver.findElement(By.cssSelector("li.active.ng-binding")).getText());
    assertTrue(isElementPresent(By.xpath("(//a[contains(text(),'Admin')])[2]")));
    assertEquals("Admin", driver.findElement(By.xpath("(//a[contains(text(),'Admin')])[2]")).getText());
    assertTrue(isElementPresent(By.linkText("Home")));
    assertEquals("Home", driver.findElement(By.linkText("Home")).getText());
    assertEquals("Edit User: Master", driver.findElement(By.xpath("//h2")).getText());
    assertEquals("User Email Address", driver.findElement(By.cssSelector("label")).getText());
    assertEquals("User Full Name", driver.findElement(By.xpath("//div[2]/label")).getText());
    assertEquals("User Phone", driver.findElement(By.xpath("//div[2]/div/label")).getText());
    assertEquals("User Permission", driver.findElement(By.xpath("//div[2]/div[2]/label")).getText());
    assertEquals("User Password", driver.findElement(By.xpath("//div[3]/div/label")).getText());
    assertEquals("Confirm User Password", driver.findElement(By.xpath("//div[3]/div[2]/label")).getText());
    assertTrue(isElementPresent(By.xpath("//input[@type='email']")));
    assertEquals("admin@nestnz.org", driver.findElement(By.xpath("//input[@type='email']")).getAttribute("value"));
    assertTrue(isElementPresent(By.xpath("//input[@type='text']")));
    assertEquals("Master", driver.findElement(By.xpath("//input[@type='text']")).getAttribute("value"));
    assertTrue(isElementPresent(By.xpath("(//input[@type='text'])[2]")));
    assertEquals("0211824040", driver.findElement(By.xpath("(//input[@type='text'])[2]")).getAttribute("value"));
    assertTrue(isElementPresent(By.xpath("//select")));
    assertTrue(isElementPresent(By.xpath("//input[@type='password']")));
    assertTrue(isElementPresent(By.xpath("(//input[@type='password'])[2]")));
    assertTrue(isElementPresent(By.xpath("//button[@type='submit ']")));
    assertEquals("Save", driver.findElement(By.xpath("//button[@type='submit ']")).getText());
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
