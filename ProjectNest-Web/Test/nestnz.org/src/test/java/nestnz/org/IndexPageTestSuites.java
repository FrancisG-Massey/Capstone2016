package nestnz.org;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.Select;

public class IndexPageTestSuites {
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
		  testIndexPageTestSuites();
	  }*/


	  @Test
	  public void LaunchTestOnFireFox() throws Exception {
		  System.setProperty("webdriver.gecko.driver",".\\Browsers\\geckodriver.exe");
	      driver = new FirefoxDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testIndexPageTestSuites();
	  }
	  @Test
	  public void LaunchTestOnChrome() throws Exception {
		  System.setProperty("webdriver.chrome.driver", ".\\Browsers\\chromedriver.exe");
	      driver = new ChromeDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testIndexPageTestSuites();
	  }
	  
private void testIndexPageTestSuites() throws Exception {
	/* Index page is displayed across all other pages.
	 * This test includes the logo image element, texts, link elements present
	 * on the navbar in index page */
	driver.get("https://www.nestnz.org");
    assertTrue(isElementPresent(By.id("navbar")));
    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | null | ]]
    assertTrue(isElementPresent(By.cssSelector("img.hidden-xs")));
    assertEquals("NEST NZ", driver.findElement(By.cssSelector("span > span")).getText());
    assertEquals("Ōhanga o Aotearoa", driver.findElement(By.cssSelector("em")).getText());
    assertTrue(isElementPresent(By.linkText("About")));
    assertEquals("About", driver.findElement(By.linkText("About")).getText());
    assertTrue(isElementPresent(By.linkText("Mobile App")));
    assertEquals("Mobile App", driver.findElement(By.linkText("Mobile App")).getText());
    assertTrue(isElementPresent(By.linkText("Statistics")));
    assertEquals("Statistics", driver.findElement(By.linkText("Statistics")).getText());
    assertTrue(isElementPresent(By.linkText("Volunteer")));
    assertEquals("Volunteer", driver.findElement(By.linkText("Volunteer")).getText());
    assertTrue(isElementPresent(By.linkText("Contact")));
    assertEquals("Contact", driver.findElement(By.linkText("Contact")).getText());
    assertTrue(isElementPresent(By.linkText("Log In")));
    assertEquals("Log In", driver.findElement(By.linkText("Log In")).getText());
    //assertTrue(isElementPresent(By.cssSelector("header.business-header.ng-scope")));
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
