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
import org.openqa.selenium.support.ui.Sleeper;

public class TrapPageTestSuites {
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
		  testTrapPageTestSuites();
	  }*/


	  @Test
	  public void LaunchTestOnFireFox() throws Exception {
		  System.setProperty("webdriver.gecko.driver",".\\Browsers\\geckodriver.exe");
	      driver = new FirefoxDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testTrapPageTestSuites();
	  }
	  @Test
	  public void LaunchTestOnChrome() throws Exception {
		  System.setProperty("webdriver.chrome.driver", ".\\Browsers\\chromedriver.exe");
	      driver = new ChromeDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testTrapPageTestSuites();
	  }
  private void testTrapPageTestSuites() throws Exception {
    driver.get(baseUrl + "/#/");
    driver.findElement(By.linkText("Log In")).click();
    driver.findElement(By.id("username")).clear();
    driver.findElement(By.id("username")).sendKeys("mjdev");
    driver.findElement(By.id("password")).clear();
    driver.findElement(By.id("password")).sendKeys("mjdevadmin");
    driver.findElement(By.xpath("//button[@type='submit']")).click();
    driver.findElement(By.linkText("View")).click();
    Thread.sleep(500);
    assertTrue(isElementPresent(By.id("addButton")));
    assertTrue(isElementPresent(By.xpath("(//button[@type='button'])[2]")));
    assertTrue(isElementPresent(By.cssSelector("th")));
    assertEquals("Trap Number", driver.findElement(By.cssSelector("th")).getText());
    assertTrue(isElementPresent(By.xpath("//th[2]")));
    assertEquals("Location", driver.findElement(By.xpath("//th[2]")).getText());
    assertTrue(isElementPresent(By.xpath("//th[3]")));
    assertEquals("Status", driver.findElement(By.xpath("//th[3]")).getText());
    assertTrue(isElementPresent(By.xpath("//th[4]")));
    assertEquals("Last Reset", driver.findElement(By.xpath("//th[4]")).getText());
    assertTrue(isElementPresent(By.xpath("//th[5]")));
    assertEquals("Placed Date", driver.findElement(By.xpath("//th[5]")).getText());
    assertTrue(isElementPresent(By.xpath("//th[6]")));
    assertEquals("Catch History", driver.findElement(By.xpath("//th[6]")).getText());
    assertTrue(isElementPresent(By.xpath("//th[7]")));
    assertEquals("Action", driver.findElement(By.xpath("//th[7]")).getText());
    assertTrue(isElementPresent(By.linkText("View")));
    assertTrue(isElementPresent(By.linkText("Edit")));
    assertEquals("View", driver.findElement(By.linkText("View")).getText());
    assertEquals("Edit", driver.findElement(By.linkText("Edit")).getText());
    assertEquals("1", driver.findElement(By.linkText("1")).getText());
    assertEquals("« Prev", driver.findElement(By.linkText("« Prev")).getText());
    assertEquals("Next »", driver.findElement(By.linkText("Next »")).getText());
    assertTrue(isElementPresent(By.cssSelector("li.active.ng-binding")));
    assertTrue(isElementPresent(By.xpath("(//a[contains(text(),'Admin')])[2]")));
    assertTrue(isElementPresent(By.linkText("Home")));
    assertEquals("Trap Admin", driver.findElement(By.cssSelector("h2.text-center.page-header")).getText());
    assertEquals("View Map", driver.findElement(By.xpath("(//button[@type='button'])[2]")).getText());
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
