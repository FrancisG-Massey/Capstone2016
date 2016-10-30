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

public class UserPageTestSuites {
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
		  testUserPageTestSuites();
	  }*/

	  @Test
	  public void LaunchTestOnFireFox() throws Exception {
		  System.setProperty("webdriver.gecko.driver",".\\Browsers\\geckodriver.exe");
	      driver = new FirefoxDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testUserPageTestSuites();
	  }
	  @Test
	  public void LaunchTestOnChrome() throws Exception {
		  System.setProperty("webdriver.chrome.driver", ".\\Browsers\\chromedriver.exe");
	      driver = new ChromeDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testUserPageTestSuites();
	  }
  private void testUserPageTestSuites() throws Exception {
    driver.get(baseUrl + "/#/");
    driver.findElement(By.linkText("Log In")).click();
    driver.findElement(By.id("username")).clear();
    driver.findElement(By.id("username")).sendKeys("mj@nestnz.org");
    driver.findElement(By.id("password")).clear();
    driver.findElement(By.id("password")).sendKeys("mjdevadmin");
    driver.findElement(By.xpath("//button[@type='submit']")).click();
    driver.findElement(By.linkText("Admin")).click();
    driver.findElement(By.linkText("Users")).click();
    assertEquals("Users", driver.findElement(By.cssSelector("h2.page-header.text-center")).getText());
    assertTrue(isElementPresent(By.id("addButton")));
    assertEquals("Add User", driver.findElement(By.id("addButton")).getText());
    assertTrue(isElementPresent(By.cssSelector("th")));
    assertEquals("Full Name", driver.findElement(By.cssSelector("th")).getText());
    assertTrue(isElementPresent(By.xpath("//th[2]")));
    assertEquals("Email", driver.findElement(By.xpath("//th[2]")).getText());
    assertTrue(isElementPresent(By.xpath("//th[3]")));
    assertEquals("Start Date", driver.findElement(By.xpath("//th[3]")).getText());
    assertTrue(isElementPresent(By.xpath("//th[4]")));
    assertEquals("Permission", driver.findElement(By.xpath("//th[4]")).getText());
    assertTrue(isElementPresent(By.xpath("//th[5]")));
    assertEquals("Trapline", driver.findElement(By.xpath("//th[5]")).getText());
    assertTrue(isElementPresent(By.xpath("//th[6]")));
    assertEquals("Action", driver.findElement(By.xpath("//th[6]")).getText());
    assertTrue(isElementPresent(By.linkText("View Traplines")));
    assertEquals("View Traplines", driver.findElement(By.linkText("View Traplines")).getText());
    assertTrue(isElementPresent(By.linkText("Edit")));
    assertEquals("Edit", driver.findElement(By.linkText("Edit")).getText());
    assertEquals("Admin", driver.findElement(By.cssSelector("ul.breadcrumb.ng-scope > li.active")).getText());
    assertTrue(isElementPresent(By.linkText("Home")));
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
