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

public class UserViewTraplinesPageTestSuites {
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
		  testUserViewTraplinesPageTestSuites();
	  }*/

	  @Test
	  public void LaunchTestOnFireFox() throws Exception {
		  System.setProperty("webdriver.gecko.driver",".\\Browsers\\geckodriver.exe");
	      driver = new FirefoxDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testUserViewTraplinesPageTestSuites();
	  }
	  @Test
	  public void LaunchTestOnChrome() throws Exception {
		  System.setProperty("webdriver.chrome.driver", ".\\Browsers\\chromedriver.exe");
	      driver = new ChromeDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testUserViewTraplinesPageTestSuites();
	  }
	  
  private void testUserViewTraplinesPageTestSuites() throws Exception {
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
    driver.findElement(By.linkText("View Traplines")).click();
    Thread.sleep(500);
    assertEquals("View / Add Traplines for admin@nestnz.org", driver.findElement(By.cssSelector("h4.modal-title.ng-binding")).getText());
    assertEquals("View / Add Traplines", driver.findElement(By.cssSelector("h3.text-center.page-header")).getText());
    assertEquals("Registered Traplines", driver.findElement(By.cssSelector("strong")).getText());
    assertTrue(isElementPresent(By.cssSelector("#tblHead > tr > th")));
    assertEquals("Trapline", driver.findElement(By.cssSelector("#tblHead > tr > th")).getText());
    assertTrue(isElementPresent(By.xpath("//thead[@id='tblHead']/tr/th[2]")));
    assertEquals("Start", driver.findElement(By.xpath("//thead[@id='tblHead']/tr/th[2]")).getText());
    assertTrue(isElementPresent(By.xpath("//thead[@id='tblHead']/tr/th[3]")));
    assertEquals("End", driver.findElement(By.xpath("//thead[@id='tblHead']/tr/th[3]")).getText());
    assertTrue(isElementPresent(By.xpath("//thead[@id='tblHead']/tr/th[4]")));
    assertEquals("Role", driver.findElement(By.xpath("//thead[@id='tblHead']/tr/th[4]")).getText());
    assertTrue(isElementPresent(By.cssSelector("a.btn.btn-info")));
    assertEquals("Add", driver.findElement(By.cssSelector("a.btn.btn-info")).getText());
    assertTrue(isElementPresent(By.cssSelector("button.btn.btn-danger")));
    assertEquals("Close", driver.findElement(By.cssSelector("button.btn.btn-danger")).getText());
    driver.findElement(By.cssSelector("button.close")).click();
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
