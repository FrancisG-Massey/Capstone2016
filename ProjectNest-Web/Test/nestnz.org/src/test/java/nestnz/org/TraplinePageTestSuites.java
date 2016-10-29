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

public class TraplinePageTestSuites {
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
		  testTraplinePageTestSuites();
	  }*/


	  @Test
	  public void LaunchTestOnFireFox() throws Exception {
		  System.setProperty("webdriver.gecko.driver",".\\Browsers\\geckodriver.exe");
	      driver = new FirefoxDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testTraplinePageTestSuites();
	  }
	  @Test
	  public void LaunchTestOnChrome() throws Exception {
		  System.setProperty("webdriver.chrome.driver", ".\\Browsers\\chromedriver.exe");
	      driver = new ChromeDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testTraplinePageTestSuites();
	  }
  private void testTraplinePageTestSuites() throws Exception {
    driver.get("https://www.nestnz.org");
    driver.findElement(By.linkText("Log In")).click();
    driver.findElement(By.id("username")).clear();
    driver.findElement(By.id("username")).sendKeys("mjdev");
    driver.findElement(By.id("password")).clear();
    driver.findElement(By.id("password")).sendKeys("mjdevadmin");
    driver.findElement(By.xpath("//button[@type='submit']")).click();
    assertEquals("Available Regions", driver.findElement(By.cssSelector("h2.page-header.text-center")).getText());
    assertTrue(isElementPresent(By.id("addButton")));
    assertEquals("Add Trap Line", driver.findElement(By.id("addButton")).getText());
    // ERROR: Caught exception [ERROR: Unsupported command [getTable | //table.0.0 | ]]
    assertTrue(isElementPresent(By.cssSelector("strong.ng-binding")));
    assertEquals("Trapline Name", driver.findElement(By.cssSelector("th")).getText());
    // ERROR: Caught exception [ERROR: Unsupported command [getTable | //table.0.1 | ]]
    assertEquals("Start", driver.findElement(By.xpath("//th[2]")).getText());
    // ERROR: Caught exception [ERROR: Unsupported command [getTable | //table.0.2 | ]]
    assertEquals("End", driver.findElement(By.xpath("//th[3]")).getText());
    // ERROR: Caught exception [ERROR: Unsupported command [getTable | //table.0.3 | ]]
    assertEquals("Traps", driver.findElement(By.xpath("//th[4]")).getText());
    // ERROR: Caught exception [ERROR: Unsupported command [getTable | //table.0.4 | ]]
    assertEquals("Volunteers", driver.findElement(By.xpath("//th[5]")).getText());
    // ERROR: Caught exception [ERROR: Unsupported command [getTable | //table.0.5 | ]]
    assertEquals("Catch History", driver.findElement(By.xpath("//th[6]")).getText());
    // ERROR: Caught exception [ERROR: Unsupported command [getTable | //table.0.6 | ]]
    assertEquals("Action", driver.findElement(By.xpath("//th[7]")).getText());
    assertTrue(isElementPresent(By.linkText("View")));
    assertTrue(isElementPresent(By.xpath("(//a[contains(text(),'View')])[2]")));
    assertTrue(isElementPresent(By.xpath("(//a[contains(text(),'View')])[3]")));
    assertTrue(isElementPresent(By.linkText("Edit")));
    assertTrue(isElementPresent(By.linkText("Home")));
    assertEquals("Admin", driver.findElement(By.cssSelector("ul.breadcrumb.ng-scope > li.active")).getText());
    assertEquals("Home", driver.findElement(By.linkText("Home")).getText());
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
