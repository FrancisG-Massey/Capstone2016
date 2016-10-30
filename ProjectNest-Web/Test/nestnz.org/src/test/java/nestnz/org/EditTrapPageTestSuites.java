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

public class EditTrapPageTestSuites {
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
		  testEditTrapPageTestSuites();
	  }*/

	  @Test
	  public void LaunchTestOnFireFox() throws Exception {
		  System.setProperty("webdriver.gecko.driver",".\\Browsers\\geckodriver.exe");
	      driver = new FirefoxDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testEditTrapPageTestSuites();
	  }
	  @Test
	  public void LaunchTestOnChrome() throws Exception {
		  System.setProperty("webdriver.chrome.driver", ".\\Browsers\\chromedriver.exe");
	      driver = new ChromeDriver();
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	      testEditTrapPageTestSuites();
	  }

  private void testEditTrapPageTestSuites() throws Exception {
    driver.get(baseUrl + "/#/");
    driver.findElement(By.linkText("Log In")).click();
    driver.findElement(By.id("username")).clear();
    driver.findElement(By.id("username")).sendKeys("mjdev");
    driver.findElement(By.id("password")).clear();
    driver.findElement(By.id("password")).sendKeys("mjdevadmin");
    driver.findElement(By.xpath("//button[@type='submit']")).click();
    Thread.sleep(500);
    driver.findElement(By.linkText("View")).click();
    Thread.sleep(500);
    driver.findElement(By.linkText("Edit")).click();
    Thread.sleep(1000);
    assertEquals("Edit Trap 1", driver.findElement(By.xpath("//h2")).getText());
    assertEquals("Latitude", driver.findElement(By.cssSelector("div.col-md-6.form-group > label")).getText());
    assertEquals("Longitude", driver.findElement(By.xpath("//div[2]/label")).getText());
    assertEquals("Bait type", driver.findElement(By.xpath("//div[3]/div/label")).getText());
    assertEquals("Trap type", driver.findElement(By.xpath("//div[3]/div[2]/label")).getText());
    assertEquals("-40.309037", driver.findElement(By.xpath("//input[@type='number']")).getAttribute("value"));
    assertEquals("175.772448", driver.findElement(By.xpath("(//input[@type='number'])[2]")).getAttribute("value"));
    assertTrue(isElementPresent(By.xpath("//button[@type='submit']")));
    assertEquals("Save", driver.findElement(By.xpath("//button[@type='submit']")).getText());
    assertEquals("Trap 1", driver.findElement(By.cssSelector("li.active.ng-binding")).getText());
    assertTrue(isElementPresent(By.linkText("Manawatu Gorge Tawa Loop")));
    assertTrue(isElementPresent(By.xpath("(//a[contains(text(),'Admin')])[2]")));
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
