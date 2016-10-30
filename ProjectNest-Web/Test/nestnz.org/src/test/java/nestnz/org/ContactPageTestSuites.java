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

public class ContactPageTestSuites {
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
		  testContactPageTestSuites();
	  }*/


	  @Test
	  public void LaunchTestOnFireFox() throws Exception {
		  System.setProperty("webdriver.gecko.driver",".\\Browsers\\geckodriver.exe");
	      driver = new FirefoxDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testContactPageTestSuites();
	  }
	  @Test
	  public void LaunchTestOnChrome() throws Exception {
		  System.setProperty("webdriver.chrome.driver", ".\\Browsers\\chromedriver.exe");
	      driver = new ChromeDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testContactPageTestSuites();
	  }
	  
  private void testContactPageTestSuites() throws Exception {
	driver.get("https://www.nestnz.org");
    driver.findElement(By.linkText("Contact")).click();
    assertEquals("Contact Us", driver.findElement(By.cssSelector("b")).getText());
    assertTrue(isElementPresent(By.cssSelector("b")));
    assertTrue(isElementPresent(By.cssSelector("img[alt=\"Address:\"]")));
    assertTrue(isElementPresent(By.cssSelector("img[alt=\"Email:\"]")));
    assertTrue(isElementPresent(By.cssSelector("img[alt=\"Github repo:\"]")));
    assertEquals("Capstone Team Two", driver.findElement(By.cssSelector("strong")).getText());
    assertTrue(isElementPresent(By.linkText("projectnestnz@gmail.com")));
    assertTrue(isElementPresent(By.linkText("visit our code repository")));
    assertEquals("projectnestnz@gmail.com", driver.findElement(By.linkText("projectnestnz@gmail.com")).getText());
    assertEquals("visit our code repository", driver.findElement(By.linkText("visit our code repository")).getText());
    assertEquals("Icons made by Freepik and Pixel Buddha from www.flaticon.com is licensed by CC 3.0 BY", driver.findElement(By.cssSelector("i")).getText());
    assertTrue(isElementPresent(By.linkText("Freepik")));
    assertTrue(isElementPresent(By.linkText("Pixel Buddha")));
    assertTrue(isElementPresent(By.linkText("www.flaticon.com")));
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
