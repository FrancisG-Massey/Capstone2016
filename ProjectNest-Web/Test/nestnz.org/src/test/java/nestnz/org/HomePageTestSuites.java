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

public class HomePageTestSuites {
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
	  testHomePageTestSuites();
  }*/

  @Test
  public void LaunchTestOnFireFox() throws Exception {
	  System.setProperty("webdriver.gecko.driver",".\\Browsers\\geckodriver.exe");
      driver = new FirefoxDriver();
      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
      testHomePageTestSuites();
  }
  @Test
  public void LaunchTestOnChrome() throws Exception {
	  System.setProperty("webdriver.chrome.driver", ".\\Browsers\\chromedriver.exe");
      driver = new ChromeDriver();
      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
      testHomePageTestSuites();
  }
  
  private void testHomePageTestSuites() throws Exception {
	  /* Contents & three image elements are tested for home page */
	driver.get("https://www.nestnz.org");
    assertTrue(isElementPresent(By.xpath("//div/div/div/a/img")));
    assertEquals("Mobile Application", driver.findElement(By.cssSelector("strong.ng-binding")).getText());
    assertEquals("We will describe our mobile app here and give information on how to download it.", driver.findElement(By.cssSelector("p")).getText());
    assertTrue(isElementPresent(By.xpath("//div[2]/a/img")));
    assertEquals("Data", driver.findElement(By.xpath("//div[2]/h2/strong")).getText());
    assertEquals("View the data from the traplines where DOC volunteers use our services", driver.findElement(By.xpath("//div[2]/div/p/strong")).getText());
    assertTrue(isElementPresent(By.xpath("//div[3]/a/img")));
    assertEquals("Volunteers", driver.findElement(By.xpath("//div[3]/h2/strong")).getText());
    //assertEquals("View information on how to sign up as a DOC volunteer.", driver.findElement(By.xpath("//div[3]/div/p/strong")).getText());
  }

  @After
  public void tearDown() throws Exception {
    driver.quit();
    driver = null;
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
