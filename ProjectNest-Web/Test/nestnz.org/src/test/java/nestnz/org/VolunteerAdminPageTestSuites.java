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

public class VolunteerAdminPageTestSuites {
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
		  testVolunteerAdminPageTestSuites();
	  }*/


	  @Test
	  public void LaunchTestOnFireFox() throws Exception {
		  System.setProperty("webdriver.gecko.driver",".\\Browsers\\geckodriver.exe");
	      driver = new FirefoxDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testVolunteerAdminPageTestSuites();
	  }
	  @Test
	  public void LaunchTestOnChrome() throws Exception {
		  System.setProperty("webdriver.chrome.driver", ".\\Browsers\\chromedriver.exe");
	      driver = new ChromeDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testVolunteerAdminPageTestSuites();
	  }
  private void testVolunteerAdminPageTestSuites() throws Exception {
    driver.get(baseUrl + "/#/");
    driver.findElement(By.linkText("Log In")).click();
    driver.findElement(By.id("username")).clear();
    driver.findElement(By.id("username")).sendKeys("mjdev");
    driver.findElement(By.id("password")).clear();
    driver.findElement(By.id("password")).sendKeys("mjdevadmin");
    driver.findElement(By.xpath("//button[@type='submit']")).click();
    driver.findElement(By.xpath("(//a[contains(text(),'View')])[2]")).click();
    Thread.sleep(500);
    assertEquals("Volunteer Admin", driver.findElement(By.cssSelector("h2.text-center.page-header")).getText());
    assertTrue(isElementPresent(By.cssSelector("th")));
    assertTrue(isElementPresent(By.xpath("//th[2]")));
    assertTrue(isElementPresent(By.xpath("//th[3]")));
    assertTrue(isElementPresent(By.xpath("//th[4]")));
    assertTrue(isElementPresent(By.xpath("//th[5]")));
    assertEquals("ID", driver.findElement(By.cssSelector("th")).getText());
    assertEquals("Full Name", driver.findElement(By.xpath("//th[2]")).getText());
    assertEquals("Email", driver.findElement(By.xpath("//th[3]")).getText());
    assertEquals("Start Date", driver.findElement(By.xpath("//th[4]")).getText());
    assertEquals("Admin", driver.findElement(By.xpath("//td[5]")).getText());
    assertTrue(isElementPresent(By.cssSelector("li.active.ng-binding")));
    assertTrue(isElementPresent(By.xpath("(//a[contains(text(),'Admin')])[2]")));
    assertTrue(isElementPresent(By.linkText("Home")));
    assertTrue(isElementPresent(By.linkText("1")));
    assertTrue(isElementPresent(By.linkText("« Prev")));
    assertTrue(isElementPresent(By.linkText("Next »")));
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
